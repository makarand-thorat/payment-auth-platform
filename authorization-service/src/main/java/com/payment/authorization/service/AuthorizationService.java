package com.payment.authorization.service;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.payment.authorization.dto.AuthorizationResult;
import com.payment.authorization.dto.TransactionEvent;
import com.payment.authorization.kafka.AuthorizationResultProducer;
import com.payment.authorization.model.AuthorizationAudit;
import com.payment.authorization.repository.AuthorizationAuditRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
	
	private final AuthorizationResultProducer resultProducer;
	private final AuthorizationAuditRepository auditRepository;
	
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
        saveAuditRecord(event, result);

        log.info("Transaction {} decision: {}",
                event.getTransactionId(), result.getDecision());

        resultProducer.publishResult(result);
    }
    
    private void saveAuditRecord(TransactionEvent event,
            AuthorizationResult result) {
        try {
            AuthorizationAudit audit = new AuthorizationAudit(
                    event.getTransactionId(),
                    event.getCardNumber(),
                    event.getAmountInCents(),
                    result.getDecision(),
                    result.getReason(),
                    null,
                    null,
                    Instant.now(),
                    Instant.now()
            );
            auditRepository.save(audit);
            log.info("Audit record saved for transaction {}",
                    event.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to save audit record for transaction {}: {}",
                    event.getTransactionId(), e.getMessage());
        }
    }

}
