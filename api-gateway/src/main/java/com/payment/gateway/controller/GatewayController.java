package com.payment.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.gateway.dto.TransactionRequest;
import com.payment.gateway.dto.TransactionResponse;
import com.payment.gateway.service.GatewayService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/authorize")
@RequiredArgsConstructor
public class GatewayController {
	
	private final GatewayService gatewayService;
	
	@PostMapping
	public ResponseEntity<TransactionResponse> authorize(
			@Valid @RequestBody TransactionRequest request){
		return ResponseEntity.ok(gatewayService.authorize(request));
	}

}
