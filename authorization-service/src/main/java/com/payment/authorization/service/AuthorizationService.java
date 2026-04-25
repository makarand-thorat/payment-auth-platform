package com.payment.authorization.service;
import com.payment.authorization.aggregation.TransactionAggregationStore;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.payment.authorization.aggregation.PendingTransaction;
import com.payment.authorization.client.AccountServiceClient;
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
	private static final int FRAUD_SCORE_THRESHOLD = 70;
	private final AuthorizationResultProducer resultProducer;
	private final AuthorizationAuditRepository auditRepository;
	private final AccountServiceClient accountServiceClient;
	private final TransactionAggregationStore aggregationStore;
	
    public void processTransaction(TransactionEvent event) {
        log.info("Processing transaction {} for card ending in {}",
                event.getTransactionId(),
                event.getCardNumber().substring(event.getCardNumber().length() - 4));
    }

        public void makeDecision(PendingTransaction pending) {
            String transactionId = pending.getTransactionId();
            log.info("Making final decision for transaction {}", transactionId);

            String decision;
            String reason;

            // Check rules first
            if (!pending.getRulesResult().getPassed()) {
                decision = "DECLINED";
                reason = "Rules check failed: "
                        + pending.getRulesResult().getReason();
            }
            // Then check fraud score
            else if (pending.getFraudScore().getScore() >= FRAUD_SCORE_THRESHOLD) {
                decision = "DECLINED";
                reason = "High fraud risk score: "
                        + pending.getFraudScore().getScore()
                        + " signals: " + pending.getFraudScore().getSignals();
            }
            // All clear
            else {
                decision = "APPROVED";
                reason = "All checks passed. Fraud score: "
                        + pending.getFraudScore().getScore();
            }
            
        

            log.info("Transaction {} final decision: {} reason: {}",
                    transactionId, decision, reason);
            if ("APPROVED".equals(decision)) {
                try {
                    accountServiceClient.debitAccount(
                            pending.getRulesResult().getCardNumber(),
                            pending.getRulesResult().getAmountInCents());
                } catch (Exception e) {
                    log.error("Failed to debit account for transaction {} — {}",
                            transactionId, e.getMessage());
                    decision = "DECLINED";
                    reason = "Payment processing failed — " + e.getMessage();
                }
            }

            AuthorizationResult result = new AuthorizationResult(
                    transactionId,
                    pending.getRulesResult().getCardNumber(),
                    pending.getRulesResult().getAmountInCents(),
                    decision,
                    reason,
                    LocalDateTime.now()
            );

            // Save to Cassandra
            saveAuditRecord(pending, decision, reason);

            // Publish final result
            resultProducer.publishResult(result);
        }
    
        private void saveAuditRecord(PendingTransaction pending,
                String decision, String reason) {
            try {
                AuthorizationAudit audit = new AuthorizationAudit(
                        pending.getTransactionId(),
                        pending.getRulesResult().getCardNumber(),
                        pending.getRulesResult().getAmountInCents(),
                        decision,
                        reason,
                        pending.getFraudScore().getScore(),
                        pending.getRulesResult().getPassed(),
                        Instant.now(),
                        Instant.now()
                );
                auditRepository.save(audit);
                log.info("Audit record saved for transaction {}",
                        pending.getTransactionId());
            } catch (Exception e) {
                log.error("Failed to save audit record: {}", e.getMessage());
            }
        }
        
        @Scheduled(fixedDelay = 60000) // runs every 60 seconds
        public void handleTimeouts() {
            List<PendingTransaction> expired = aggregationStore.getAndRemoveExpired();

            for (PendingTransaction pending : expired) {
                log.warn("Transaction {} timed out waiting for signals — "
                        + "rulesReceived={} fraudReceived={}",
                        pending.getTransactionId(),
                        pending.getRulesResult() != null,
                        pending.getFraudScore() != null);

                AuthorizationResult result = new AuthorizationResult(
                        pending.getTransactionId(),
                        pending.getTransactionId(), // cardNumber may be null
                        null,
                        "DECLINED",
                        "Transaction timed out waiting for authorization signals",
                        LocalDateTime.now()
                );

                // Try to save audit record
                try {
                    AuthorizationAudit audit = new AuthorizationAudit(
                            pending.getTransactionId(),
                            pending.getRulesResult() != null
                                    ? pending.getRulesResult().getCardNumber()
                                    : "UNKNOWN",
                            pending.getRulesResult() != null
                                    ? pending.getRulesResult().getAmountInCents()
                                    : 0L,
                            "DECLINED",
                            "TIMEOUT",
                            pending.getFraudScore() != null
                                    ? pending.getFraudScore().getScore()
                                    : null,
                            pending.getRulesResult() != null
                                    ? pending.getRulesResult().getPassed()
                                    : null,
                            Instant.now(),
                            Instant.now()
                    );
                    auditRepository.save(audit);
                } catch (Exception e) {
                    log.error("Failed to save timeout audit record: {}",
                            e.getMessage());
                }

                resultProducer.publishResult(result);
            }
        }

}
