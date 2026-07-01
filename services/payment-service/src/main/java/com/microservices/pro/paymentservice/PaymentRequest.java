package com.microservices.pro.paymentservice;

import java.math.BigDecimal;

/**
 * PaymentRequest — Session 4.
 */
public record PaymentRequest(BigDecimal amount) {}
