package com.microservices.pro.paymentservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * PaymentSagaHandler — Session 7.
 *
 * Implement the TODO below. See docs/labs/session-07-lab-5a.md.
 */
@Service
public class PaymentSagaHandler {

    @Autowired
    private PaymentProcessor paymentProcessor;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // TODO: @KafkaListener(topics = "inventory-events", groupId = "payment-service")
    //       Handle InventoryReservedEvent ONLY — ignore other events on this
    //       topic (check rawEvent.contains("InventoryReserved") first, return
    //       early otherwise):
    //         - Call paymentProcessor.processPayment(orderId)
    //         - On success: publish PaymentCompletedEvent to "payment-events"
    //         - On PaymentException: publish PaymentFailedEvent
    public void handleInventoryReserved(String rawEvent) {
        throw new UnsupportedOperationException("TODO: implement handleInventoryReserved()");
    }
}
