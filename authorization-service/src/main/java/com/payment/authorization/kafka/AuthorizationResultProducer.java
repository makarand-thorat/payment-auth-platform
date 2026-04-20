package com.payment.authorization.kafka;

import com.payment.authorization.dto.AuthorizationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationResultProducer {

    private static final String TOPIC = "transaction.result";

    private final KafkaTemplate<String, AuthorizationResult> kafkaTemplate;

    public void publishResult(AuthorizationResult result) {
        kafkaTemplate.send(TOPIC, result.getTransactionId(), result)
                .whenComplete((r, ex) -> {
                    if (ex == null) {
                        log.info("Authorization result {} published to topic {}",
                                result.getTransactionId(), TOPIC);
                    } else {
                        log.error("Failed to publish result {}: {}",
                                result.getTransactionId(), ex.getMessage());
                    }
                });
    }
}