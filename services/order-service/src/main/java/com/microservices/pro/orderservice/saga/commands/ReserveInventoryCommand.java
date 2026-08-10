package com.microservices.pro.orderservice.saga.commands;

/**
 * ReserveInventoryCommand — Session 12.
 * Published to: "saga-commands" by OrderSagaOrchestrator.
 * Consumed by: InventorySagaCommandHandler (filter: commandType == "ReserveInventoryCommand").
 */
public record ReserveInventoryCommand(
        String commandType,
        String orderId,
        String productId,
        int quantity
) {
    public ReserveInventoryCommand(String orderId, String productId, int quantity) {
        this("ReserveInventoryCommand", orderId, productId, quantity);
    }
}
