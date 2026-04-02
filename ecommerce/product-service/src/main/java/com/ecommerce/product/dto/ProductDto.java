package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Request to create or update a product")
    public static class ProductRequest {

        @NotBlank(message = "Product code is required")
        @Schema(description = "Unique product code", example = "PROD-101")
        private String productCode;

        @NotBlank(message = "Name is required")
        @Size(max = 255)
        @Schema(description = "Product name", example = "Wireless Headphones")
        private String name;

        @Size(max = 1000)
        @Schema(description = "Product description")
        private String description;

        @NotNull @DecimalMin("0.01")
        @Schema(description = "Product price", example = "49.99")
        private BigDecimal price;

        @NotNull @Min(0)
        @Schema(description = "Stock quantity", example = "100")
        private Integer stockQuantity;

        @NotBlank
        @Schema(description = "Category", example = "Electronics")
        private String category;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Product response")
    public static class ProductResponse {
        private Long id;
        private String productCode;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private String category;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Stock update request")
    public static class StockUpdateRequest {

        @NotNull
        @Schema(description = "Quantity to add (positive) or remove (negative)", example = "10")
        private Integer delta;
    }
}
