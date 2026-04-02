package com.ecommerce.order.kafka;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResultConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "${kafka.topics.payment-result}", groupId = "order-service")
    @Transactional
    public void handlePaymentResult(Map<String, Object> payload) {
        String orderNumber = (String) payload.get("orderNumber");
        boolean success = Boolean.TRUE.equals(payload.get("success"));

        log.info("Received payment result for order {}: {}", orderNumber, success ? "SUCCESS" : "FAILED");

        orderRepository.findByOrderNumber(orderNumber).ifPresent(order -> {
            order.setStatus(success
                ? Order.OrderStatus.CONFIRMED
                : Order.OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            log.info("Updated order {} status to {}", orderNumber, order.getStatus());
        });
    }
}
