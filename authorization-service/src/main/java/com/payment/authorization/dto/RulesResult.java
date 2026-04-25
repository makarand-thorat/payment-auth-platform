package com.payment.authorization.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RulesResult {
    private String transactionId;
    private String cardNumber;
    private Long amountInCents;
    private Boolean passed;
    private String reason;
    private LocalDateTime decidedAt;
}
