package com.payment.authorization.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.payment.authorization.dto.TransactionEvent;
import com.payment.authorization.service.AuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {
	
	private final AuthorizationService authorizationService;
	
	@KafkaListener(
            topics = "transaction.submitted",
            groupId = "authorization-service"
    )
	  public void consume(TransactionEvent event) {
        log.info("Received transaction event: {}", event.getTransactionId());
        authorizationService.processTransaction(event);
    }
	

}
