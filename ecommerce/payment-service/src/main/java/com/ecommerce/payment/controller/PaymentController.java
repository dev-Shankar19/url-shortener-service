package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Service", description = "Initiate, retrieve, and refund payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
        summary = "Initiate a payment",
        description = "Manually initiate payment for an order. " +
                      "Publishes a payment.result Kafka event to update the Order Service."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment initiated",
            content = @Content(schema = @Schema(implementation = PaymentDto.PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Payment already exists for this order")
    })
    public ResponseEntity<PaymentDto.PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentDto.PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentService.initiatePayment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found",
            content = @Content(schema = @Schema(implementation = PaymentDto.PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentDto.PaymentResponse> getPayment(
            @Parameter(description = "Payment ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/order/{orderNumber}")
    @Operation(summary = "Get payment by order number")
    public ResponseEntity<PaymentDto.PaymentResponse> getPaymentByOrder(
            @Parameter(description = "Order number", example = "ORD-20240101-ABC12345")
            @PathVariable String orderNumber) {
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all payments for a customer")
    public ResponseEntity<List<PaymentDto.PaymentResponse>> getPaymentsByCustomer(
            @Parameter(description = "Customer ID", example = "CUST-001")
            @PathVariable String customerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerId));
    }

    @PostMapping("/{paymentReference}/refund")
    @Operation(
        summary = "Refund a payment",
        description = "Marks a successful payment as REFUNDED. Only SUCCESS status payments are eligible."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment refunded"),
        @ApiResponse(responseCode = "400", description = "Payment not eligible for refund"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentDto.PaymentResponse> refundPayment(
            @Parameter(description = "Payment reference", example = "PAY-ABC123456789")
            @PathVariable String paymentReference) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentReference));
    }
}
