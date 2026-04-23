package com.payment.fraud.kafka;

import com.payment.fraud.dto.TransactionEvent;
import com.payment.fraud.service.FraudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {

    private final FraudService fraudService;

    @KafkaListener(
            topics = "transaction.submitted",
            groupId = "fraud-service"
    )
    public void consume(TransactionEvent event) {
        log.info("Received transaction event: {}",
                event.getTransactionId());
        fraudService.assessFraud(event);
    }
}