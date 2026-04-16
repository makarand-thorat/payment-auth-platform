package com.payment.gateway.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Long amountInCents;

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotBlank(message = "Merchant category is required")
    private String merchantCategory;
    
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
}
