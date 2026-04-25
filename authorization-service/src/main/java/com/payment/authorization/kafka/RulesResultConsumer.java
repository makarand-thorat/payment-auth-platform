package com.payment.authorization.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.payment.authorization.aggregation.TransactionAggregationStore;
import com.payment.authorization.dto.RulesResult;
import com.payment.authorization.service.AuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RulesResultConsumer {

    private final TransactionAggregationStore aggregationStore;
    private final AuthorizationService authorizationService;

    @KafkaListener(
            topics = "rules.result",
            groupId = "authorization-service-rules"
    )
    public void consume(RulesResult result) {
        log.info("Received rules result for transaction {} passed={}",
                result.getTransactionId(), result.getPassed());

        aggregationStore.storeRulesResult(result);

        if (aggregationStore.isComplete(result.getTransactionId())) {
            authorizationService.makeDecision(
                    aggregationStore.getAndRemove(result.getTransactionId()));
        }
    }
}