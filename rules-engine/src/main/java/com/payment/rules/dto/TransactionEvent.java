package com.payment.rules.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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