package com.microservices.pro.inventoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.pro.inventoryservice.events.InventoryReleasedEvent;
import com.microservices.pro.inventoryservice.events.InventoryReservationFailedEvent;
import com.microservices.pro.inventoryservice.events.InventoryReservedEvent;
import com.microservices.pro.inventoryservice.events.OrderPlacedEvent;
import com.microservices.pro.inventoryservice.events.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * InventorySagaHandler — Session 7.
 *
 * Two @KafkaListener methods, two DIFFERENT consumer groups — deliberate,
 * see Session 7 docx Common Issues table ("Compensation fires but stock
 * not released" → "Check consumer group names — inventory-compensation
 * must be a DIFFERENT group from inventory-service to avoid one consumer
 * processing both"):
 *   - "order-events",   group "inventory-service"        — reserve stock
 *   - "payment-events", group "inventory-compensation"   — release stock
 *     (compensation, triggered by PaymentFailed)
 *
 * See OrderSagaEventHandler.java for the full list of all five group IDs
 * used across this Saga.
 */
@Service
public class InventorySagaHandler {

    private static final Logger log = LoggerFactory.getLogger(InventorySagaHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Step 2 of the Saga: reserve inventory when an order is placed.
     */
    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("[SAGA] Handling OrderPlaced for order: {}", event.orderId());
        try {
            inventoryService.reserveStock(event.productId(), event.quantity(), event.orderId());
            kafkaTemplate.send("inventory-events", event.orderId(),
                    new InventoryReservedEvent(event.orderId(), event.productId(), event.quantity()));
            log.info("[SAGA] Inventory reserved for order: {}", event.orderId());
        } catch (InsufficientStockException e) {
            kafkaTemplate.send("inventory-events", event.orderId(),
                    new InventoryReservationFailedEvent(event.orderId(), e.getMessage()));
            log.warn("[SAGA] Inventory reservation FAILED for order: {}", event.orderId());
        }
    }

    /**
     * Compensation: release inventory when payment fails. MUST use a
     * different consumer group than handleOrderPlaced() above — see
     * class-level note.
     */
    @KafkaListener(topics = "payment-events", groupId = "inventory-compensation")
    public void handlePaymentFailed(String rawEvent) {
        if (!rawEvent.contains("PaymentFailed")) {
            return; // ignore PaymentCompleted and any other event on this topic
        }
        PaymentFailedEvent event = parsePaymentFailed(rawEvent);
        inventoryService.releaseStock(event.orderId());  // undo the reservation
        kafkaTemplate.send("inventory-events", event.orderId(),
                new InventoryReleasedEvent(event.orderId()));
        log.info("[SAGA] COMPENSATION: Inventory released for order: {}", event.orderId());
    }

    private PaymentFailedEvent parsePaymentFailed(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, PaymentFailedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PaymentFailedEvent: " + rawJson, e);
        }
    }
}
