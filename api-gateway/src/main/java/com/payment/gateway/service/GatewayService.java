package com.payment.gateway.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.payment.gateway.client.AccountServiceClient;
import com.payment.gateway.dto.AccountResponse;
import com.payment.gateway.dto.TransactionEvent;
import com.payment.gateway.dto.TransactionRequest;
import com.payment.gateway.dto.TransactionResponse;
import com.payment.gateway.kafka.TransactionProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayService {
	
	private final AccountServiceClient accountServiceClient;
	private final TransactionProducer transactionProducer;
	
	public TransactionResponse authorize(TransactionRequest request, String correlationId) {
	    String transactionId = UUID.randomUUID().toString();
	    log.info("Processing transaction {} correlationId {} for card {}",
	            transactionId, correlationId, maskCard(request.getCardNumber()));

	    AccountResponse account = accountServiceClient.getAccount(request.getCardNumber());

	    if ("BLOCKED".equals(account.getStatus())) {
	        log.warn("Transaction {} correlationId {} declined - account blocked",
	                transactionId, correlationId);
	        return new TransactionResponse(
	                transactionId,
	                request.getCardNumber(),
	                request.getAmountInCents(),
	                "DECLINED",
	                "Account is blocked"
	        );
	    }

	    if (account.getBalanceInCents() < request.getAmountInCents()) {
	        log.warn("Transaction {} correlationId {} declined - insufficient funds",
	                transactionId, correlationId);
	        return new TransactionResponse(
	                transactionId,
	                request.getCardNumber(),
	                request.getAmountInCents(),
	                "DECLINED",
	                "Insufficient funds"
	        );
	    }

	    

	    TransactionEvent event = new TransactionEvent(
	            transactionId,
	            request.getCardNumber(),
	            request.getAmountInCents(),
	            request.getMerchantId(),
	            request.getMerchantCategory(),
	            request.getTimestamp(),
	            account.getStatus(),
	            account.getBalanceInCents() - request.getAmountInCents()
	    );

	    transactionProducer.publishTransaction(event);

	    log.info("Transaction {} correlationId {} approved and published to Kafka",
	            transactionId, correlationId);

	    return new TransactionResponse(
	            transactionId,
	            request.getCardNumber(),
	            request.getAmountInCents(),
	            "PENDING",
	            "Transaction recieved and pending processing"
	    );
	}
	
	private String maskCard(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

}
