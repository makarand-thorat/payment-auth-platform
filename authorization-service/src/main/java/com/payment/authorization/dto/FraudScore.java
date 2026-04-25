package com.payment.authorization.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudScore {
    private String transactionId;
    private String cardNumber;
    private Long amountInCents;
    private int score;
    private List<String> signals;
    private LocalDateTime scoredAt;
}