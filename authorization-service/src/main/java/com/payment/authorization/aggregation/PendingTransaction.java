package com.payment.authorization.aggregation;

import com.payment.authorization.dto.FraudScore;
import com.payment.authorization.dto.RulesResult;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PendingTransaction {
    private final String transactionId;
    private RulesResult rulesResult;
    private FraudScore fraudScore;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public PendingTransaction(String transactionId) {
        this.transactionId = transactionId;
    }
}