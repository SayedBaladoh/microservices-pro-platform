package com.microservices.pro.inventoryservice;

import com.microservices.pro.inventoryservice.events.OrderPlacedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * InventorySagaTest — Session 7, Lab 5A, Task 5.
 *
 * Matches the original lab spec's required tests:
 *   Test 4: handleOrderPlaced() publishes InventoryReservedEvent when
 *           stock is available
 *   Test 5: handlePaymentFailed() calls releaseStock() and publishes
 *           InventoryReleasedEvent
 */
@ExtendWith(MockitoExtension.class)
class InventorySagaTest {

    @InjectMocks
    private InventorySagaHandler handler;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void handleOrderPlaced_publishesInventoryReservedEvent_whenStockAvailable() {
        OrderPlacedEvent event = new OrderPlacedEvent("order-123", "PROD-001", 2, new BigDecimal("200.00"), "cust-1");
        // inventoryService.reserveStock() is void and succeeds by default
        // (Mockito mocks void methods as no-ops unless told to throw).

        handler.handleOrderPlaced(event);

        verify(kafkaTemplate).send(
                eq("inventory-events"),
                eq("order-123"),
                any(com.microservices.pro.inventoryservice.events.InventoryReservedEvent.class));
    }

    @Test
    void handlePaymentFailed_callsReleaseStock_andPublishesInventoryReleasedEvent() {
        String paymentFailedJson = "{\"orderId\":\"order-123\",\"reason\":\"PaymentFailed: card declined\"}";

        handler.handlePaymentFailed(paymentFailedJson);

        verify(inventoryService).releaseStock("order-123");
        verify(kafkaTemplate).send(
                eq("inventory-events"),
                eq("order-123"),
                any(com.microservices.pro.inventoryservice.events.InventoryReleasedEvent.class));
    }
}
