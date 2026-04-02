package com.ecommerce.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback responses when a downstream service is unavailable.
 * These are triggered by the CircuitBreaker filter in application.yml.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/order")
    public Mono<ResponseEntity<Map<String, Object>>> orderFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(fallbackBody("Order Service is temporarily unavailable. Please try again later.")));
    }

    @GetMapping("/product")
    public Mono<ResponseEntity<Map<String, Object>>> productFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(fallbackBody("Product Service is temporarily unavailable. Please try again later.")));
    }

    @GetMapping("/payment")
    public Mono<ResponseEntity<Map<String, Object>>> paymentFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(fallbackBody("Payment Service is temporarily unavailable. Please try again later.")));
    }

    private Map<String, Object> fallbackBody(String message) {
        return Map.of(
            "status", 503,
            "message", message,
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
