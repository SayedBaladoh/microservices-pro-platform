package com.microservices.pro.inventoryservice.events;

/**
 * OrderCancelledEvent — Session 7.
 * The Saga's compensation-path terminal event.
 */
public record OrderCancelledEvent(String orderId, String reason) {}
