package com.payment.account.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebitResponse {
    private String cardNumber;
    private Long newBalanceInCents;
    private String message;
}
