package com.payment.gateway.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader(value = "X-Correlation-ID",
                    required = false) String correlationId) {

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        TransactionResponse response = gatewayService.authorize(request, correlationId);
        return ResponseEntity.ok()
                .header("X-Correlation-ID", correlationId)
                .body(response);
    }

}
