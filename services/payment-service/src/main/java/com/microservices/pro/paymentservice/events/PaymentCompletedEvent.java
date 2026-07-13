package com.microservices.pro.paymentservice.events;

/**
 * PaymentCompletedEvent — Session 7.
 * Published by Payment Service to "payment-events" on successful payment.
 */
public record PaymentCompletedEvent(String orderId, String transactionId) {}
