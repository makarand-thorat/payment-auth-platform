package com.payment.authorization.aggregation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.payment.authorization.dto.FraudScore;
import com.payment.authorization.dto.RulesResult;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransactionAggregationStore {
	
	 private final Map<String, PendingTransaction> pendingTransactions
     = new ConcurrentHashMap<>();
	 
	    public void storeRulesResult(RulesResult result) {
	        String txId = result.getTransactionId();
	        pendingTransactions.computeIfAbsent(txId,
	                k -> new PendingTransaction(txId))
	                .setRulesResult(result);
	        log.info("Stored rules result for transaction {}", txId);
	    }
	    
	    public void storeFraudScore(FraudScore score) {
	        String txId = score.getTransactionId();
	        pendingTransactions.computeIfAbsent(txId,
	                k -> new PendingTransaction(txId))
	                .setFraudScore(score);
	        log.info("Stored fraud score for transaction {}", txId);
	    }
	    
	    public boolean isComplete(String transactionId) {
	        PendingTransaction pending = pendingTransactions.get(transactionId);
	        return pending != null
	                && pending.getRulesResult() != null
	                && pending.getFraudScore() != null;
	    }
	    
	    public PendingTransaction getAndRemove(String transactionId) {
	        return pendingTransactions.remove(transactionId);
	    }
	    
	    
	    public List<PendingTransaction> getAndRemoveExpired() {
	        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
	        List<PendingTransaction> expired = new ArrayList<>();

	        pendingTransactions.entrySet().removeIf(entry -> {
	            if (entry.getValue().getCreatedAt().isBefore(cutoff)) {
	                expired.add(entry.getValue());
	                return true;
	            }
	            return false;
	        });

	        return expired;
	    }

}
