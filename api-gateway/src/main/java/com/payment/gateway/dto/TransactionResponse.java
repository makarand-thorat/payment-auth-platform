package com.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String cardNumber;
    private Long amountInCents;
    private String status;
    private String message;
}
