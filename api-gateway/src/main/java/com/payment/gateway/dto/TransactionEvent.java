package com.payment.gateway.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
    private String transactionId;
    private String cardNumber;
    private Long amountInCents;
    private String merchantId;
    private String merchantCategory;
    private LocalDateTime timestamp;
    private String accountStatus;
    private Long balanceInCents;
}
