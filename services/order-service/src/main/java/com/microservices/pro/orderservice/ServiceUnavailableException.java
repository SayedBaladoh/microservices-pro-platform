package com.microservices.pro.orderservice;

/**
 * ServiceUnavailableException — Session 6.
 * Thrown by InventoryErrorDecoder when Inventory Service returns 503.
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
