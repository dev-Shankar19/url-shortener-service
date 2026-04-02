package com.ecommerce.payment.dto;

import com.ecommerce.payment.model.Payment.PaymentMethod;
import com.ecommerce.payment.model.Payment.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Manual payment initiation request")
    public static class PaymentRequest {

        @NotBlank(message = "Order number is required")
        @Schema(description = "Order number to pay for", example = "ORD-20240101-ABC12345")
        private String orderNumber;

        @NotBlank(message = "Customer ID is required")
        @Schema(description = "Customer identifier", example = "CUST-001")
        private String customerId;

        @NotNull @DecimalMin("0.01")
        @Schema(description = "Payment amount", example = "99.98")
        private BigDecimal amount;

        @NotNull
        @Schema(description = "Payment method")
        private PaymentMethod paymentMethod;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Payment response")
    public static class PaymentResponse {
        private Long id;
        private String paymentReference;
        private String orderNumber;
        private String customerId;
        private BigDecimal amount;
        private PaymentStatus status;
        private PaymentMethod paymentMethod;
        private String failureReason;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
    }

    // Internal Kafka event consumed from order-service
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderCreatedEvent {
        private String orderNumber;
        private String customerId;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
    }

    // Internal Kafka event published back to order-service
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentResultEvent {
        private String orderNumber;
        private String paymentReference;
        private boolean success;
        private String failureReason;
    }
}
