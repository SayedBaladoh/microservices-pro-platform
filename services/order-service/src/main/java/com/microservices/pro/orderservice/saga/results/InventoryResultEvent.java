package com.microservices.pro.orderservice.saga.results;

/**
 * InventoryResultEvent — Session 12, Lab 10A Task 1.
 *
 * Published by InventorySagaCommandHandler → Orchestrator.
 * topic: "saga-results"
 * success=true  → reservation confirmed
 * success=false → reservation failed (reason explains why)
 */
public record InventoryResultEvent(String orderId, boolean success, String reason) {}
