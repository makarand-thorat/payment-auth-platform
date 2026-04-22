package com.payment.rules.service;



import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.payment.rules.client.AccountServiceClient;
import com.payment.rules.dto.AccountResponse;
import com.payment.rules.dto.RulesResult;
import com.payment.rules.dto.TransactionEvent;
import com.payment.rules.kafka.RulesResultProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RulesService {
	
	private final AccountServiceClient accountServiceClient;
	private final RulesResultProducer rulesResultProducer;
	
	@Value("${rules.max-single-transaction-cents}")
	private Long maxSingleTransactionCents;
	
	@Value("${rules.blocked-categories}")
	private String blockedCategoriesRaw;
	
	@Value("${rules.velocity-window-minutes}")
	private int velocityWindowMinutes;
	
	@Value("${rules.velocity-max-different-merchants}")
	private int velocityMaxDifferentMerchants;
	
	private  Map<String, List<VelocityRecord>> velocityStore
    = new ConcurrentHashMap<>();
	
	

	public void processRules (TransactionEvent event) {
		
		List<String> blockedCategories = List.of(blockedCategoriesRaw.split(","));
		
		log.info("Processing transaction {} for card ending in {}",
                event.getTransactionId(),
                event.getCardNumber().substring(event.getCardNumber().length() - 4));
		
		AccountResponse account = accountServiceClient.getAccount(event.getCardNumber());
        if (account.getDailyLimitCents() < event.getAmountInCents()) {
            decline(event, "Amount exceeds daily limit");
            return;
        }

        // Rule 2 — blocked merchant category
        if (blockedCategories.contains(event.getMerchantCategory())) {
            decline(event, "Merchant category "
                    + event.getMerchantCategory() + " is blocked");
            return;
        }

        // Rule 3 — single transaction amount ceiling
        if (event.getAmountInCents() > maxSingleTransactionCents) {
            decline(event, "Single transaction amount exceeds maximum of "
                    + maxSingleTransactionCents + " cents");
            return;
        }

        // Rule 4 — geographic velocity check
        // More than N different merchant IDs within X minutes = suspicious
        if (isVelocityBreached(event)) {
            decline(event, "Too many different merchants in a short time window");
            return;
        }

        
        recordVelocity(event);
	    
	    log.info("Transaction {} decision: Passed",
                event.getTransactionId());
        
        rulesResultProducer.publishResult(new RulesResult
        		(
	                 event.getTransactionId(),
	                 event.getCardNumber(),
	                 event.getAmountInCents(),
	                 true,
	                 null,
	                 LocalDateTime.now()
	         ));
	    

	        
	    
		

		
	    
	}
	   private boolean isVelocityBreached(TransactionEvent event) {
	        String cardNumber = event.getCardNumber();
	        LocalDateTime windowStart = LocalDateTime.now()
	                .minusMinutes(velocityWindowMinutes);

	        List<VelocityRecord> records = velocityStore
	                .getOrDefault(cardNumber, new CopyOnWriteArrayList<>());

	        // Remove expired records outside the time window
	        records.removeIf(r -> r.timestamp.isBefore(windowStart));

	        // Count distinct merchant IDs in the window
	        long distinctMerchants = records.stream()
	                .map(r -> r.merchantId)
	                .distinct()
	                .count();

	        if (distinctMerchants >= velocityMaxDifferentMerchants) {
	            log.warn("Velocity breach for card ending in {} — {} different"
	                    + " merchants in last {} minutes",
	                    cardNumber.substring(cardNumber.length() - 4),
	                    distinctMerchants,
	                    velocityWindowMinutes);
	            return true;
	        }

	        return false;
	    }
	   
	   private void recordVelocity(TransactionEvent event) {
	        velocityStore.computeIfAbsent(event.getCardNumber(),
	                k -> new CopyOnWriteArrayList<>())
	                .add(new VelocityRecord(
	                        event.getMerchantId(),
	                        LocalDateTime.now()
	                ));
	    }
	   
	    private void decline(TransactionEvent event, String reason) {
	        log.warn("Transaction {} declined — {}",
	                event.getTransactionId(), reason);
	        rulesResultProducer.publishResult(new RulesResult(
	                event.getTransactionId(),
	                event.getCardNumber(),
	                event.getAmountInCents(),
	                false,
	                reason,
	                LocalDateTime.now()
	        ));
	    }
	    
	    private static class VelocityRecord {
	        final String merchantId;
	        final LocalDateTime timestamp;

	        VelocityRecord(String merchantId, LocalDateTime timestamp) {
	            this.merchantId = merchantId;
	            this.timestamp = timestamp;
	        }
	    }
}
