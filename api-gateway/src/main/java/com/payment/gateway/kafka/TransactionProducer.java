package com.payment.gateway.kafka;

import com.payment.gateway.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private static final String TOPIC = "transaction.submitted";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void publishTransaction(TransactionEvent event) {
        kafkaTemplate.send(TOPIC, event.getTransactionId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Transaction {} published to Kafka topic {}",
                                event.getTransactionId(), TOPIC);
                    } else {
                        log.error("Failed to publish transaction {} to Kafka: {}",
                                event.getTransactionId(), ex.getMessage());
                    }
                });
    }
}