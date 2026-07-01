package com.microservices.pro.orderservice;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * PaymentResponse — Session 4.
 *
 * Deliberately duplicated from payment-service's own PaymentResponse record
 * — see PaymentRequest.java in this package for the rationale.
 */
public record PaymentResponse(String transactionId, BigDecimal amount) {

    public PaymentResponse(BigDecimal amount) {
        this(UUID.randomUUID().toString(), amount);
    }
}
