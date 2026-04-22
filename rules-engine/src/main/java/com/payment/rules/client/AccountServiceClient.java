package com.payment.rules.client;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.payment.rules.dto.AccountResponse;
import com.payment.rules.dto.DebitRequest;
import com.payment.rules.dto.DebitResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountServiceClient {
	
	private final RestTemplate restTemplate;
	
	@Value("${account-service.url}")
	private String  accountServiceUrl;
	
	public AccountResponse getAccount(String cardNumber) {
		
		try {
			String url = accountServiceUrl + "/accounts/" + cardNumber;
			return restTemplate.getForObject(url, AccountResponse.class);
		}catch (HttpClientErrorException.NotFound e) {
			throw new RuntimeException("Account not found: " + cardNumber);
		}catch (Exception e) {
			throw new RuntimeException("Account service unavailable");
		}
	}
	
	public void debitAccount(String cardNumber, Long amountInCents) {
	    try {
	        String url = accountServiceUrl + "/accounts/" + cardNumber + "/debit";
	        DebitRequest request = new DebitRequest(amountInCents);
	        restTemplate.postForObject(url, request, DebitResponse.class);
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to debit account: " + e.getMessage());
	    }
	}

}
