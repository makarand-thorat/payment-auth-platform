package com.payment.authorization.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.time.Instant;

@Table("authorization_audit")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationAudit {

    @PrimaryKey
    @Column("transaction_id")
    private String transactionId;

    @Column("card_number")
    private String cardNumber;

    @Column("amount_in_cents")
    private Long amountInCents;

    @Column("decision")
    private String decision;

    @Column("fail_reason")
    private String failReason;

    @Column("fraud_score")
    private Integer fraudScore;

    @Column("rules_passed")
    private Boolean rulesPassed;

    @Column("decided_at")
    private Instant decidedAt;

    @Column("created_at")
    private Instant createdAt;
}