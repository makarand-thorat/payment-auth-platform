package com.payment.authorization.kafka;

import com.payment.authorization.dto.AuthorizationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResultConsumerTest {

    @KafkaListener(
            topics = "transaction.result",
            groupId = "result-test-consumer"
    )
    public void consume(AuthorizationResult result) {
        log.info("TEST CONSUMER - Result received: transactionId={} decision={} reason={}",
                result.getTransactionId(),
                result.getDecision(),
                result.getReason());
    }
}