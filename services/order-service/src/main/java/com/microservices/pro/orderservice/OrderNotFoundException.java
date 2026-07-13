package com.microservices.pro.orderservice;

/**
 * OrderNotFoundException — Session 7.
 * Thrown by GET /api/orders/{orderId}/status for an unknown orderId.
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
