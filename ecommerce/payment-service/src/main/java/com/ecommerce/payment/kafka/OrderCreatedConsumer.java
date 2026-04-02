package com.ecommerce.payment.kafka;

import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.Payment.PaymentMethod;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consumes order.created events and auto-initiates payment processing.
 * In a real system this would integrate with a payment gateway (Razorpay, Stripe, etc.)
 * Here we simulate success/failure with a simple random check.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentResultProducer resultProducer;

    @KafkaListener(topics = "${kafka.topics.order-created}", groupId = "payment-service")
    @Transactional
    public void handleOrderCreated(PaymentDto.OrderCreatedEvent event) {
        log.info("Received order.created event for order: {}", event.getOrderNumber());

        // Idempotency check — don't process twice
        if (paymentRepository.findByOrderNumber(event.getOrderNumber()).isPresent()) {
            log.warn("Payment already exists for order: {}", event.getOrderNumber());
            return;
        }

        // Simulate payment gateway call (90% success rate for demo)
        boolean paymentSuccess = Math.random() > 0.1;
        String failureReason = paymentSuccess ? null : "Simulated gateway decline";

        Payment payment = Payment.builder()
            .paymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
            .orderNumber(event.getOrderNumber())
            .customerId(event.getCustomerId())
            .amount(event.getTotalAmount())
            .paymentMethod(PaymentMethod.CREDIT_CARD) // default for auto-processing
            .status(paymentSuccess ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED)
            .failureReason(failureReason)
            .processedAt(LocalDateTime.now())
            .build();

        paymentRepository.save(payment);
        log.info("Processed payment {} for order {}: {}",
            payment.getPaymentReference(), event.getOrderNumber(),
            paymentSuccess ? "SUCCESS" : "FAILED");

        // Publish result back to order-service
        resultProducer.publishPaymentResult(PaymentDto.PaymentResultEvent.builder()
            .orderNumber(event.getOrderNumber())
            .paymentReference(payment.getPaymentReference())
            .success(paymentSuccess)
            .failureReason(failureReason)
            .build());
    }
}
