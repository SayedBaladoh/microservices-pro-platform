package com.microservices.pro.orderservice.events;

/**
 * PaymentFailedEvent — Session 7.
 * Published by Payment Service to "payment-events" when payment fails.
 * Triggers compensation: Inventory releases the reservation, Order is cancelled.
 */
public record PaymentFailedEvent(String orderId, String reason) {}
