package com.microservices.pro.orderservice.saga.results;

/**
 * PaymentResultEvent — Session 12, Lab 10A Task 1.
 *
 * Published by PaymentSagaCommandHandler → Orchestrator.
 * topic: "saga-results"
 * success=true  → payment confirmed; transactionId filled
 * success=false → payment failed; reason filled
 */
public record PaymentResultEvent(String orderId, boolean success, String transactionId) {}
