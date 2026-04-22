package com.payment.rules.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.payment.rules.dto.RulesResult;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RulesConsumerTest {
	
	@KafkaListener(
			topics = "rules.result",
			groupId = "rules-result-test-consumer"
			)
    public void consume(RulesResult result) {
        log.info("TEST CONSUMER - Result received: transactionId={} decision={} reason={}",
                result.getTransactionId(),
                result.getPassed(),
                result.getReason());
    }

}
