package com.microservices.pro.orderservice.events;

/**
 * InventoryReservedEvent — Session 7.
 * Published by Inventory Service to "inventory-events" on successful reservation.
 */
public record InventoryReservedEvent(String orderId, String productId, int quantity) {}
