package com.microservices.pro.orderservice;

/**
 * InsufficientStockException — Session 6.
 * Thrown by InventoryErrorDecoder when Inventory Service returns 409 Conflict.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
