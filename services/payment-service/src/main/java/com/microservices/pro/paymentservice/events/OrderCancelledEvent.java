package com.microservices.pro.paymentservice.events;

/**
 * OrderCancelledEvent — Session 7.
 * The Saga's compensation-path terminal event.
 */
public record OrderCancelledEvent(String orderId, String reason) {}
