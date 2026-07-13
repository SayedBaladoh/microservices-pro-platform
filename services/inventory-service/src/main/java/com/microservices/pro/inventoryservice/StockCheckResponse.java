package com.microservices.pro.inventoryservice;

/**
 * StockCheckResponse — Session 6.
 */
public record StockCheckResponse(
        String productId,
        int requestedQuantity,
        boolean available,
        int remainingStock
) {}
