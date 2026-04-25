package com.payment.authorization.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private String cardNumber;
    private String holderName;
    private Long balanceInCents;
    private Long dailyLimitCents;
    private String status;
}
