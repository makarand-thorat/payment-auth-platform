package com.payment.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.account.dto.AccountResponse;
import com.payment.account.dto.DebitRequest;
import com.payment.account.dto.DebitResponse;
import com.payment.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
	 private final AccountService accountService;

	 @GetMapping("/{cardNumber}")
	 public ResponseEntity<AccountResponse> getAccount(@PathVariable String cardNumber) {
	        return ResponseEntity.ok(accountService.getAccount(cardNumber));
	    }

	 @PostMapping("/{cardNumber}/debit")
	    public ResponseEntity<DebitResponse> debitAccount(
	            @PathVariable String cardNumber,
	            @RequestBody DebitRequest request) {
	        return ResponseEntity.ok(accountService.debitAccount(cardNumber, request));
	    }
}
