package com.microservices.pro.orderservice.events;

/**
 * PaymentCompletedEvent — Session 7.
 * Published by Payment Service to "payment-events" on successful payment.
 */
public record PaymentCompletedEvent(String orderId, String transactionId) {}
