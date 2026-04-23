package com.payment.fraud.kafka;

import com.payment.fraud.dto.FraudScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudScoreProducer {

    private static final String TOPIC = "fraud.score";

    private final KafkaTemplate<String, FraudScore> kafkaTemplate;

    public void publishScore(FraudScore score) {
        kafkaTemplate.send(TOPIC, score.getTransactionId(), score)
                .whenComplete((r, ex) -> {
                    if (ex == null) {
                        log.info("Fraud score {} published — score: {}",
                                score.getTransactionId(), score.getScore());
                    } else {
                        log.error("Failed to publish fraud score {}: {}",
                                score.getTransactionId(), ex.getMessage());
                    }
                });
    }
}