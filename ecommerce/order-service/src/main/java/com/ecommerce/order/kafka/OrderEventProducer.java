package com.ecommerce.order.kafka;

import com.ecommerce.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    public void publishOrderCreated(OrderDto.OrderCreatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(orderCreatedTopic, event.getOrderNumber(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published order.created event for order: {}", event.getOrderNumber());
            } else {
                log.error("Failed to publish order.created event for order: {}", event.getOrderNumber(), ex);
            }
        });
    }

    public void publishOrderCancelled(String orderNumber) {
        kafkaTemplate.send(orderCancelledTopic, orderNumber, orderNumber)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Published order.cancelled event for order: {}", orderNumber);
                } else {
                    log.error("Failed to publish order.cancelled event for order: {}", orderNumber, ex);
                }
            });
    }
}
