package com.payment.gateway.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.payment.gateway.dto.TransactionEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransactionConsumerTest {
	
	public TransactionConsumerTest() {
	    System.out.println("DEBUG: Consumer Class Instance Created!");
	}
	@KafkaListener(
			topics = "transaction.submitted",
			groupId = "gateway-test-consumer-3",
			containerFactory = "kafkaListenerContainerFactory"
			)
	public void consume(TransactionEvent event) {
		log.info("TEST CONSUMER received transaction: {} for card ending in {}",
				event.getTransactionId(),
				event.getCardNumber().substring(event.getCardNumber().length()-4));
				
	}

}
