package com.ecommerce.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

/**
 * Centralized authentication filter applied to every request through the API Gateway.
 *
 * Strategy: API-Key header validation (simple, resume-friendly).
 * In production replace with JWT Bearer token validation using Spring Security + OAuth2.
 *
 * How it works:
 *  1. All requests must carry:  X-API-Key: <configured-key>
 *  2. Public paths (Swagger, actuator, OPTIONS) are whitelisted and pass through freely.
 *  3. Any request missing or with a wrong key gets 401 Unauthorized immediately.
 *  4. Downstream services receive an X-Authenticated: true header so they can trust
 *     that the gateway already validated the caller.
 */
@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${gateway.auth.api-key:secret-api-key-change-in-prod}")
    private String expectedApiKey;

    // Paths that skip authentication (Swagger, health, CORS preflight)
    private static final List<String> PUBLIC_PATHS = List.of(
        "/swagger-ui",
        "/api-docs",
        "/actuator",
        "/fallback",
        "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Always allow OPTIONS (CORS preflight)
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Allow public paths through without auth
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }

        // Validate API key
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            log.warn("Unauthorized request to {} — invalid or missing X-API-Key", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("WWW-Authenticate", "ApiKey realm=\"ecommerce\"");
            return exchange.getResponse().setComplete();
        }

        // Pass downstream — add trusted header so services know auth passed
        log.debug("Authenticated request to {}", path);
        ServerWebExchange mutated = exchange.mutate()
            .request(r -> r.header("X-Authenticated", "true")
                           .header("X-Gateway-Source", "api-gateway"))
            .build();

        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -1; // Run before all other filters
    }
}
