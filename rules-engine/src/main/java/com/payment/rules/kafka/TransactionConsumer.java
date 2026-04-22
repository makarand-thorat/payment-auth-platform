package com.payment.rules.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.payment.rules.dto.TransactionEvent;
import com.payment.rules.service.RulesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {
	
	private final RulesService rulesService;
	
	@KafkaListener(
            topics = "transaction.submitted",
            groupId = "rules-service"
    )
	  public void consume(TransactionEvent event) {
        log.info("Received transaction event: {}", event.getTransactionId());
        rulesService.processRules(event);
    }
	

}