package com.microservices.pro.orderservice.saga.results;

/**
 * InventoryReleasedEvent — Session 12, Lab 10A Task 1.
 *
 * Published by InventorySagaCommandHandler → Orchestrator (compensation confirmed).
 * topic: "saga-results"
 */
public record InventoryReleasedEvent(String orderId) {}
