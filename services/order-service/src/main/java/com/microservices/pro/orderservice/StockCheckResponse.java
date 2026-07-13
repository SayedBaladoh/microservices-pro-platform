package com.microservices.pro.orderservice;

/**
 * StockCheckResponse — Session 6.
 *
 * Deliberately duplicated from inventory-service's own StockCheckResponse
 * record rather than shared via a common library — see PaymentRequest.java
 * in this package for the documented rationale (shared libs are Session 14).
 */
public record StockCheckResponse(
        String productId,
        int requestedQuantity,
        boolean available,
        int remainingStock
) {}
