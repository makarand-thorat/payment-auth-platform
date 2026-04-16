package com.payment.gateway.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.payment.gateway.client.AccountServiceClient;
import com.payment.gateway.dto.AccountResponse;
import com.payment.gateway.dto.TransactionRequest;
import com.payment.gateway.dto.TransactionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayService {
	
	private final AccountServiceClient accountServiceClient;
	
	public TransactionResponse authorize(TransactionRequest request) {
		String transactionId = UUID.randomUUID().toString();
		log.info("Processing transaction {} for card {}",
				transactionId,maskCard(request.getCardNumber()));
		
		AccountResponse account = accountServiceClient.getAccount(request.getCardNumber());
		
		if("BLOCKED".equals(account.getStatus())) {
			log.warn("Transaction {} declined - account blocked", transactionId);
			
			return new TransactionResponse(
                    transactionId,
                    request.getCardNumber(),
                    request.getAmountInCents(),
                    "DECLINED",
                    "Account is blocked"
					
					);
		}
		
        if (account.getBalanceInCents() < request.getAmountInCents()) {
            log.warn("Transaction {} declined - insufficient funds", transactionId);
            return new TransactionResponse(
                    transactionId,
                    request.getCardNumber(),
                    request.getAmountInCents(),
                    "DECLINED",
                    "Insufficient funds"
            );
        }
        
        accountServiceClient.debitAccount(request.getCardNumber(), request.getAmountInCents());

        log.info("Transaction {} approved and account debited", transactionId);
        return new TransactionResponse(
                transactionId,
                request.getCardNumber(),
                request.getAmountInCents(),
                "APPROVED",
                "Transaction approved"
        );
	}
	
	private String maskCard(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

}
