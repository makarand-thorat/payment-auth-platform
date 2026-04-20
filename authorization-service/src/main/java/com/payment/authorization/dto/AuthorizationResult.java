package com.payment.authorization.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationResult {
    private String transactionId;
    private String cardNumber;
    private Long amountInCents;
    private String decision;
    private String reason;
    private LocalDateTime decidedAt;
}