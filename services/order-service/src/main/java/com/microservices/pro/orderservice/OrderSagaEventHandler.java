package com.microservices.pro.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.pro.orderservice.events.InventoryReleasedEvent;
import com.microservices.pro.orderservice.events.PaymentCompletedEvent;
import com.microservices.pro.orderservice.events.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * OrderSagaEventHandler — Session 7.
 *
 * Handles the final Saga outcomes for an order. Listens on TWO different
 * topics with TWO DIFFERENT consumer groups — this is deliberate, not an
 * oversight:
 *   - "payment-events", group "order-service" — happy/sad path from Payment
 *   - "inventory-events", group "order-service-cancel" — the COMPENSATION
 *     confirmation from Inventory (InventoryReleased), which must use a
 *     DIFFERENT group than the main "order-service" group, or one consumer
 *     instance would end up competing for both message types on
 *     overlapping topics. See Session 7 docx Common Issues table:
 *     "Compensation fires but stock not released" — group name collisions
 *     are exactly the failure mode that table warns about, mirrored here
 *     on the Order Service side of the same boundary.
 *
 * Group IDs used across this Saga (verify uniqueness whenever this file or
 * its siblings are touched):
 *   order-service            (this class, payment-events)
 *   order-service-cancel     (this class, inventory-events)
 *   inventory-service        (InventorySagaHandler, order-events)
 *   inventory-compensation   (InventorySagaHandler, payment-events)
 *   payment-service          (PaymentSagaHandler, inventory-events)
 * Five distinct group IDs across the three services — no two consumers
 * that need independent copies of a topic's messages share a group.
 *
 * Event parsing note: the docx's own live-coding script parses event type
 * from the raw JSON string by string-matching ("if rawEvent.contains(...)")
 * and explicitly calls this "Simplified... In production: use proper JSON
 * deserialization with type discriminator." This repo keeps the same
 * string-matching dispatch (so the docx's teaching point about it stays
 * visible) but backs the actual field extraction with a real Jackson
 * ObjectMapper rather than a TODO stub, since this is the reference
 * implementation and needs to actually run.
 */
@Service
public class OrderSagaEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaEventHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    public void handlePaymentEvent(String rawEvent) {
        if (rawEvent.contains("PaymentCompleted")) {
            PaymentCompletedEvent event = parsePaymentCompleted(rawEvent);
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.orderId()));
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("[SAGA] Order {} CONFIRMED", event.orderId());
        } else if (rawEvent.contains("PaymentFailed")) {
            PaymentFailedEvent event = parsePaymentFailed(rawEvent);
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.orderId()));
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            log.warn("[SAGA] Order {} payment failed, waiting for inventory release...", event.orderId());
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-service-cancel")
    public void handleInventoryReleased(String rawEvent) {
        if (rawEvent.contains("InventoryReleased")) {
            InventoryReleasedEvent event = parseInventoryReleased(rawEvent);
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.orderId()));
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("[SAGA] Order {} CANCELLED — inventory released", event.orderId());
        }
    }

    private PaymentCompletedEvent parsePaymentCompleted(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, PaymentCompletedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PaymentCompletedEvent: " + rawJson, e);
        }
    }

    private PaymentFailedEvent parsePaymentFailed(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, PaymentFailedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PaymentFailedEvent: " + rawJson, e);
        }
    }

    private InventoryReleasedEvent parseInventoryReleased(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, InventoryReleasedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse InventoryReleasedEvent: " + rawJson, e);
        }
    }
}

