package com.microservices.pro.inventoryservice;

/**
 * StockItem — Session 6.
 */
public record StockItem(
        String productId,
        int availableQuantity,
        int reservedQuantity
) {
    public boolean hasStock(int requested) {
        return availableQuantity - reservedQuantity >= requested;
    }
}
