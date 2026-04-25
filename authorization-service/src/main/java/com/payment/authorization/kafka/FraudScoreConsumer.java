package com.payment.authorization.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.payment.authorization.aggregation.TransactionAggregationStore;
import com.payment.authorization.dto.FraudScore;
import com.payment.authorization.service.AuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudScoreConsumer {

    private final TransactionAggregationStore aggregationStore;
    private final AuthorizationService authorizationService;

    @KafkaListener(
            topics = "fraud.score",
            groupId = "authorization-service-fraud"

    )
    public void consume(FraudScore score) {
        log.info("Received fraud score for transaction {} score={}",
                score.getTransactionId(), score.getScore());

        aggregationStore.storeFraudScore(score);

        if (aggregationStore.isComplete(score.getTransactionId())) {
            authorizationService.makeDecision(
                    aggregationStore.getAndRemove(score.getTransactionId()));
        }
    }
}