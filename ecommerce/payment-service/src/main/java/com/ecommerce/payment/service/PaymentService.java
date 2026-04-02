package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.kafka.PaymentResultProducer;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentResultProducer resultProducer;

    /**
     * Manual payment initiation endpoint — used when the consumer UI
     * needs to retry or initiate a payment independently.
     */
    @Transactional
    public PaymentDto.PaymentResponse initiatePayment(PaymentDto.PaymentRequest request) {
        // Idempotency check
        paymentRepository.findByOrderNumber(request.getOrderNumber()).ifPresent(p -> {
            throw new IllegalStateException(
                "Payment already exists for order " + request.getOrderNumber() +
                " with status: " + p.getStatus());
        });

        // Simulate gateway
        boolean success = Math.random() > 0.1;
        String failureReason = success ? null : "Simulated payment gateway decline";

        Payment payment = Payment.builder()
            .paymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
            .orderNumber(request.getOrderNumber())
            .customerId(request.getCustomerId())
            .amount(request.getAmount())
            .paymentMethod(request.getPaymentMethod())
            .status(success ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED)
            .failureReason(failureReason)
            .processedAt(LocalDateTime.now())
            .build();

        Payment saved = paymentRepository.save(payment);

        // Notify order-service of result
        resultProducer.publishPaymentResult(PaymentDto.PaymentResultEvent.builder()
            .orderNumber(saved.getOrderNumber())
            .paymentReference(saved.getPaymentReference())
            .success(success)
            .failureReason(failureReason)
            .build());

        log.info("Manual payment {} for order {}: {}", saved.getPaymentReference(),
            saved.getOrderNumber(), success ? "SUCCESS" : "FAILED");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentDto.PaymentResponse getPayment(Long id) {
        return toResponse(paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id)));
    }

    @Transactional(readOnly = true)
    public PaymentDto.PaymentResponse getPaymentByOrder(String orderNumber) {
        return toResponse(paymentRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new EntityNotFoundException("No payment found for order: " + orderNumber)));
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentResponse> getPaymentsByCustomer(String customerId) {
        return paymentRepository.findByCustomerId(customerId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public PaymentDto.PaymentResponse refundPayment(String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentReference));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentDto.PaymentResponse toResponse(Payment p) {
        return PaymentDto.PaymentResponse.builder()
            .id(p.getId())
            .paymentReference(p.getPaymentReference())
            .orderNumber(p.getOrderNumber())
            .customerId(p.getCustomerId())
            .amount(p.getAmount())
            .status(p.getStatus())
            .paymentMethod(p.getPaymentMethod())
            .failureReason(p.getFailureReason())
            .createdAt(p.getCreatedAt())
            .processedAt(p.getProcessedAt())
            .build();
    }
}
