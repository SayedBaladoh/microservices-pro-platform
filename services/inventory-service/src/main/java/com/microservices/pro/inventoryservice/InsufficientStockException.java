package com.microservices.pro.inventoryservice;

/**
 * InsufficientStockException — Session 7.
 *
 * Thrown by InventoryService.reserveStock() when stock is no longer
 * available at the moment the Saga tries to commit the reservation.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
