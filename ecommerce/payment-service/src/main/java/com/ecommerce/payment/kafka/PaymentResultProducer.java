package com.ecommerce.payment.kafka;

import com.ecommerce.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.payment-result}")
    private String paymentResultTopic;

    public void publishPaymentResult(PaymentDto.PaymentResultEvent event) {
        kafkaTemplate.send(paymentResultTopic, event.getOrderNumber(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Published payment.result for order {}: success={}",
                        event.getOrderNumber(), event.isSuccess());
                } else {
                    log.error("Failed to publish payment.result for order {}",
                        event.getOrderNumber(), ex);
                }
            });
    }
}
