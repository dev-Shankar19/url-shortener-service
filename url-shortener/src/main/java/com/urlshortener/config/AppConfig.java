package com.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import java.time.Duration;
import java.util.List;

@Configuration
public class AppConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ─── OpenAPI / Swagger ───────────────────────────────────────────────────

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("URL Shortener API")
                .description("Scalable URL shortening service with Redis caching, Base62 encoding, " +
                             "expiration support, and click analytics.")
                .version("1.0.0")
                .contact(new Contact().name("Your Name").email("you@example.com"))
                .license(new License().name("MIT")))
            .servers(List.of(
                new Server().url(baseUrl).description("Current server")
            ));
    }

    // ─── Redis Cache ─────────────────────────────────────────────────────────

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(24))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())
            )
            .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
