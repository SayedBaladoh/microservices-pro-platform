package com.microservices.pro.orderservice;

import com.microservices.pro.orderservice.events.InventoryReleasedEvent;
import com.microservices.pro.orderservice.events.PaymentCompletedEvent;
import com.microservices.pro.orderservice.events.PaymentFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * OrderSagaEventHandler — Session 7.
 *
 * Implement the TODOs below. See docs/labs/session-07-lab-5a.md.
 *
 * CRITICAL: the two @KafkaListener annotations below MUST use DIFFERENT
 * groupId values, or compensation will silently fail to fire correctly.
 * See docs/setup/troubleshooting.md, Session 7 section.
 */
@Service
public class OrderSagaEventHandler {

    @Autowired
    private OrderRepository orderRepository;

    // TODO 1: @KafkaListener(topics = "payment-events", groupId = "order-service")
    //         Handle PaymentCompleted -> update order status to CONFIRMED
    //         Handle PaymentFailed    -> update order status to PAYMENT_FAILED
    //         (dispatch on rawEvent.contains("PaymentCompleted") / "PaymentFailed")
    public void handlePaymentEvent(String rawEvent) {
        throw new UnsupportedOperationException("TODO: implement handlePaymentEvent()");
    }

    // TODO 2: @KafkaListener(topics = "inventory-events", groupId = "order-service-cancel")
    //         Handle InventoryReleased -> update order status to CANCELLED
    //         (this MUST be a different groupId than TODO 1's listener)
    public void handleInventoryReleased(String rawEvent) {
        throw new UnsupportedOperationException("TODO: implement handleInventoryReleased()");
    }
}
