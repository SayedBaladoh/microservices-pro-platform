package com.microservices.pro.inventoryservice.events;

/**
 * InventoryReservationFailedEvent — Session 7.
 * Published by Inventory Service to "inventory-events" when reservation fails.
 */
public record InventoryReservationFailedEvent(String orderId, String reason) {}
