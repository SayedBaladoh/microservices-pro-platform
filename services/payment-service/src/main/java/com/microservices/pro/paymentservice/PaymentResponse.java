package com.microservices.pro.paymentservice;

import java.math.BigDecimal;

/**
 * PaymentResponse — Session 4.
 */
public record PaymentResponse(String transactionId, String status, BigDecimal amount) {}
