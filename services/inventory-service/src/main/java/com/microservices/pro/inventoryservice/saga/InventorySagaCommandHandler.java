package com.microservices.pro.inventoryservice.saga;

import com.microservices.pro.inventoryservice.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * InventorySagaCommandHandler — Session 12, Lab 10A, Task 1.
 *
 * Implement the TODO below. See docs/labs/session-12-lab-10a.md.
 *
 * Consumer group: "inventory-saga-handler" (distinct from all S7 groups).
 * Topic: "saga-commands" (filter by commandType field).
 */
@Service
public class InventorySagaCommandHandler {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // TODO: @KafkaListener(topics="saga-commands", groupId="inventory-saga-handler")
    //   Parse rawCommand as Map<String, Object>.
    //   Switch on commandType:
    //     "ReserveInventoryCommand" → inventoryService.reserveStock(productId, quantity, orderId)
    //                                  on success: publish InventoryResultEvent(orderId, true, "")
    //                                  on InsufficientStockException: publish InventoryResultEvent(orderId, false, reason)
    //     "ReleaseInventoryCommand" → inventoryService.releaseStock(orderId)
    //                                  publish InventoryReleasedEvent(orderId)
    //     default → ignore (not our command)
    //   Publish results to: "saga-results"
    public void handleCommand(String rawCommand) {
        throw new UnsupportedOperationException("TODO: implement handleCommand()");
    }
}
