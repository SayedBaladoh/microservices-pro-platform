package com.microservices.pro.paymentservice.events;

/**
 * InventoryReleasedEvent — Session 7.
 * Published by Inventory Service to "inventory-events" after compensation
 * (releasing a reservation following PaymentFailed).
 */
public record InventoryReleasedEvent(String orderId) {}
