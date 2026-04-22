package com.payment.rules.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.payment.rules.dto.RulesResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RulesResultProducer {
	
	private static final String TOPIC = "rules.result";
	private final KafkaTemplate<String, RulesResult> kafkaTemplate;
	
	public void publishResult(RulesResult result) {
        kafkaTemplate.send(TOPIC, result.getTransactionId(), result)
        .whenComplete((r, ex) -> {
            if (ex == null) {
                log.info("Rules result {} published to topic {}",
                        result.getTransactionId(), TOPIC);
            } else {
                log.error("Failed to publish result {}: {}",
                        result.getTransactionId(), ex.getMessage());
            }
        });
		
	}

}
