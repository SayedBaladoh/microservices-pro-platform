package com.microservices.pro.orderservice.events;

/**
 * OrderConfirmedEvent — Session 7.
 * The Saga's happy-path terminal event (used by the documented bonus
 * Notification Service homework — not consumed by anything in this repo's
 * current scope, included for fidelity with the docx's domain event list).
 */
public record OrderConfirmedEvent(String orderId, String transactionId) {}
