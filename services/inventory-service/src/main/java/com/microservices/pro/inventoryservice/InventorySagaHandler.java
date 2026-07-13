package com.microservices.pro.inventoryservice;

import com.microservices.pro.inventoryservice.events.OrderPlacedEvent;
import com.microservices.pro.inventoryservice.events.PaymentFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * InventorySagaHandler — Session 7.
 *
 * Implement the TODOs below. See docs/labs/session-07-lab-5a.md.
 *
 * CRITICAL: the two @KafkaListener annotations below MUST use DIFFERENT
 * groupId values, or compensation will silently fail to fire correctly
 * (one consumer instance ends up competing for both message types). See
 * docs/setup/troubleshooting.md, Session 7 section.
 */
@Service
public class InventorySagaHandler {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // TODO 1: @KafkaListener(topics = "order-events", groupId = "inventory-service")
    //         Handle OrderPlacedEvent:
    //           - Call inventoryService.reserveStock()
    //           - On success: publish InventoryReservedEvent to "inventory-events"
    //           - On InsufficientStockException: publish InventoryReservationFailedEvent
    public void handleOrderPlaced(OrderPlacedEvent event) {
        throw new UnsupportedOperationException("TODO: implement handleOrderPlaced()");
    }

    // TODO 2: @KafkaListener(topics = "payment-events", groupId = "inventory-compensation")
    //         [COMPENSATION] Handle PaymentFailedEvent only (ignore other
    //         events on this topic — check rawEvent.contains("PaymentFailed")):
    //           - Call inventoryService.releaseStock(orderId)
    //           - Publish InventoryReleasedEvent to "inventory-events"
    public void handlePaymentFailed(String rawEvent) {
        throw new UnsupportedOperationException("TODO: implement handlePaymentFailed()");
    }
}
