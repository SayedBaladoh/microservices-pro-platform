package com.microservices.pro.orderservice;

/**
 * ProductNotFoundException — Session 6.
 * Thrown by InventoryErrorDecoder when Inventory Service returns 404 Not Found.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
