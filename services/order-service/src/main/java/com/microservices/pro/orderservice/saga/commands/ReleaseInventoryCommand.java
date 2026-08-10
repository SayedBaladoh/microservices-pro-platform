package com.microservices.pro.orderservice.saga.commands;

/**
 * ReleaseInventoryCommand — Session 12.
 * Published to: "saga-commands" by OrderSagaOrchestrator (compensation step).
 * Consumed by: InventorySagaCommandHandler (filter: commandType == "ReleaseInventoryCommand").
 */
public record ReleaseInventoryCommand(
        String commandType,
        String orderId,
        String productId,
        int quantity
) {
    public ReleaseInventoryCommand(String orderId, String productId, int quantity) {
        this("ReleaseInventoryCommand", orderId, productId, quantity);
    }
}
