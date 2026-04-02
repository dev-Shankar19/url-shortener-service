package com.ecommerce.order.dto;

import com.ecommerce.order.model.Order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Request to place a new order")
    public static class CreateOrderRequest {

        @NotBlank(message = "Customer ID is required")
        @Schema(description = "Customer identifier", example = "CUST-001")
        private String customerId;

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        @Schema(description = "List of items to order")
        private List<OrderItemRequest> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Single item in an order")
    public static class OrderItemRequest {

        @NotBlank(message = "Product ID is required")
        @Schema(description = "Product identifier", example = "PROD-101")
        private String productId;

        @NotBlank(message = "Product name is required")
        @Schema(description = "Product name", example = "Wireless Headphones")
        private String productName;

        @NotNull @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Quantity", example = "2")
        private Integer quantity;

        @NotNull @DecimalMin(value = "0.01", message = "Price must be positive")
        @Schema(description = "Unit price", example = "49.99")
        private BigDecimal unitPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Order response")
    public static class OrderResponse {

        @Schema(description = "Order ID", example = "1")
        private Long id;

        @Schema(description = "Order number", example = "ORD-20240101-001")
        private String orderNumber;

        @Schema(description = "Customer ID", example = "CUST-001")
        private String customerId;

        @Schema(description = "Order status")
        private OrderStatus status;

        @Schema(description = "Order items")
        private List<OrderItemResponse> items;

        @Schema(description = "Total amount", example = "99.98")
        private BigDecimal totalAmount;

        @Schema(description = "Created timestamp")
        private LocalDateTime createdAt;

        @Schema(description = "Last updated timestamp")
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Order item in response")
    public static class OrderItemResponse {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Kafka event emitted when order is created")
    public static class OrderCreatedEvent {
        private String orderNumber;
        private String customerId;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
    }
}
