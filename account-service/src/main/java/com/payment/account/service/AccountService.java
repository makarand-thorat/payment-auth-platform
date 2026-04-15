package com.payment.account.service;


import org.springframework.stereotype.Service;

import com.payment.account.dto.AccountResponse;
import com.payment.account.dto.DebitRequest;
import com.payment.account.dto.DebitResponse;
import com.payment.account.model.Account;
import com.payment.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	
	private final AccountRepository accountRepository;
	
	public AccountResponse getAccount(String cardNumber) {
		 Account account = accountRepository.findByCardNumber(cardNumber)
				 .orElseThrow(() -> new RuntimeException("Account not found:" + cardNumber));
		 
		 return new AccountResponse(
	                account.getCardNumber(),
	                account.getHolderName(),
	                account.getBalanceInCents(),
	                account.getDailyLimitCents(),
	                account.getStatus()
				 );
				 
	}
	
	
	public DebitResponse debitAccount(String cardNumber, DebitRequest request) {
		Account account = accountRepository.findByCardNumber(cardNumber)
				 .orElseThrow(() -> new RuntimeException("Account not found:" + cardNumber));
		if ("BLOCKED".equals(account.getStatus())) {
            throw new RuntimeException("Account is blocked: " + cardNumber);
        }
		 if (request.getAmountInCents() <= 0) {
	            throw new RuntimeException("Amount must be greater than zero");
	        }
		 
		 if (account.getBalanceInCents() < request.getAmountInCents()) {
	            throw new RuntimeException("Insufficient funds");
	        }
		 
		 account.setBalanceInCents(account.getBalanceInCents() - request.getAmountInCents());
		 accountRepository.save(account);
		 
		 return new DebitResponse(
				 account.getCardNumber(),
				 account.getBalanceInCents(),
				 "Debit Successful"
			
				 );
	}
	

}
