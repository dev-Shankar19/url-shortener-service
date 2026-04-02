package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class UrlDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Request to shorten a URL")
    public static class ShortenRequest {

        @NotBlank(message = "URL must not be blank")
        @Pattern(regexp = "^(https?://).*", message = "URL must start with http:// or https://")
        @Size(max = 2048, message = "URL must be under 2048 characters")
        @Schema(description = "The original URL to shorten", example = "https://www.example.com/very/long/path")
        private String originalUrl;

        @Size(max = 20, message = "Custom alias max 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Alias can only contain letters, digits, - and _")
        @Schema(description = "Optional custom alias", example = "my-link")
        private String customAlias;

        @Min(value = 1, message = "Expiry must be at least 1 day")
        @Max(value = 365, message = "Expiry max 365 days")
        @Schema(description = "Expiry in days (default: 30)", example = "30")
        private Integer expiryDays;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Shortened URL response")
    public static class ShortenResponse {

        @Schema(description = "Short URL", example = "http://localhost:8080/abc123")
        private String shortUrl;

        @Schema(description = "Short code", example = "abc123")
        private String shortCode;

        @Schema(description = "Original URL", example = "https://www.example.com/very/long/path")
        private String originalUrl;

        @Schema(description = "Expiry date/time")
        private LocalDateTime expiresAt;

        @Schema(description = "Creation timestamp")
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "URL analytics data")
    public static class UrlAnalytics {

        @Schema(description = "Short code", example = "abc123")
        private String shortCode;

        @Schema(description = "Original URL")
        private String originalUrl;

        @Schema(description = "Total click count")
        private Long clickCount;

        @Schema(description = "Creation timestamp")
        private LocalDateTime createdAt;

        @Schema(description = "Expiry date/time")
        private LocalDateTime expiresAt;

        @Schema(description = "Is active")
        private boolean active;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Standard API error response")
    public static class ErrorResponse {
        @Schema(description = "HTTP status code")
        private int status;

        @Schema(description = "Error message")
        private String message;

        @Schema(description = "Timestamp")
        private LocalDateTime timestamp;
    }
}
