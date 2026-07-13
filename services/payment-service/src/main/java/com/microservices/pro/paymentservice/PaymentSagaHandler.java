package com.microservices.pro.paymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.pro.paymentservice.events.InventoryReservedEvent;
import com.microservices.pro.paymentservice.events.PaymentCompletedEvent;
import com.microservices.pro.paymentservice.events.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * PaymentSagaHandler — Session 7.
 *
 * Consumer group "payment-service" on "inventory-events" — explicitly
 * ignores every event on that topic except InventoryReserved (the topic
 * also carries InventoryReservationFailed and InventoryReleased, which are
 * not this service's concern).
 *
 * See OrderSagaEventHandler.java for the full list of all five group IDs
 * used across this Saga.
 */
@Service
public class PaymentSagaHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PaymentProcessor paymentProcessor;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Step 3 of the Saga: process payment once inventory is reserved.
     */
    @KafkaListener(topics = "inventory-events", groupId = "payment-service")
    public void handleInventoryReserved(String rawEvent) {
        if (!rawEvent.contains("InventoryReserved")) {
            return; // ignore InventoryReservationFailed / InventoryReleased on this topic
        }
        InventoryReservedEvent event = parseInventoryReserved(rawEvent);
        log.info("[SAGA] Processing payment for order: {}", event.orderId());
        try {
            String txId = paymentProcessor.processPayment(event.orderId());
            kafkaTemplate.send("payment-events", event.orderId(),
                    new PaymentCompletedEvent(event.orderId(), txId));
            log.info("[SAGA] Payment COMPLETED for order: {}", event.orderId());
        } catch (PaymentException e) {
            kafkaTemplate.send("payment-events", event.orderId(),
                    new PaymentFailedEvent(event.orderId(), e.getMessage()));
            log.warn("[SAGA] Payment FAILED for order: {} — triggering compensation", event.orderId());
        }
    }

    private InventoryReservedEvent parseInventoryReserved(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, InventoryReservedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse InventoryReservedEvent: " + rawJson, e);
        }
    }
}
