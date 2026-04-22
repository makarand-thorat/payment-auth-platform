package com.payment.rules.service;



import java.time.LocalDateTime;

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

	public void processRules (TransactionEvent event) {
		log.info("Processing transaction {} for card ending in {}",
                event.getTransactionId(),
                event.getCardNumber().substring(event.getCardNumber().length() - 4));
		
		AccountResponse account = accountServiceClient.getAccount(event.getCardNumber());

	    if (account.getDailyLimitCents() < event.getAmountInCents()) {
	    	log.warn("Declined transaction {} for card ending in {} : Daily limit expired",
	                event.getTransactionId(),
	                event.getCardNumber().substring(event.getCardNumber().length() - 4));
	        
	        rulesResultProducer.publishResult(new RulesResult
	        		(
	                 event.getTransactionId(),
	                 event.getCardNumber(),
	                 event.getAmountInCents(),
	                 false,
	                 "Daily limit expired",
	                 LocalDateTime.now()
	         ));

	        return;
	    	
	    	
	    }
	    
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
}
