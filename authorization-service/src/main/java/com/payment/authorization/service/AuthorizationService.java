package com.payment.authorization.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.payment.authorization.dto.AuthorizationResult;
import com.payment.authorization.dto.TransactionEvent;
import com.payment.authorization.kafka.AuthorizationResultProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
	
	private final AuthorizationResultProducer resultProducer;
	
    public void processTransaction(TransactionEvent event) {
        log.info("Processing transaction {} for card ending in {}",
                event.getTransactionId(),
                event.getCardNumber().substring(event.getCardNumber().length() - 4));

        AuthorizationResult result = new AuthorizationResult(
                event.getTransactionId(),
                event.getCardNumber(),
                event.getAmountInCents(),
                "APPROVED",
                "Hardcoded approval - rules engine not yet connected",
                LocalDateTime.now()
        );

        log.info("Transaction {} decision: {}",
                event.getTransactionId(), result.getDecision());

        resultProducer.publishResult(result);
    }

}
