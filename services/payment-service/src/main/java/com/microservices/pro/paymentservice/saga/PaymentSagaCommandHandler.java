package com.microservices.pro.paymentservice.saga;

import com.microservices.pro.paymentservice.PaymentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * PaymentSagaCommandHandler — Session 12, Lab 10A, Task 1.
 *
 * Implement the TODO below. See docs/labs/session-12-lab-10a.md.
 *
 * Consumer group: "payment-saga-handler" (distinct from all S7 groups).
 * Topic: "saga-commands" (filter: commandType == "ProcessPaymentCommand" only).
 */
@Service
public class PaymentSagaCommandHandler {

    @Autowired
    private PaymentProcessor paymentProcessor;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // TODO: @KafkaListener(topics="saga-commands", groupId="payment-saga-handler")
    //   Parse rawCommand as Map<String, Object>.
    //   If commandType != "ProcessPaymentCommand" → return (ignore)
    //   Call paymentProcessor.processPayment(orderId):
    //     on success: publish PaymentResultEvent(orderId, true, transactionId)
    //     on PaymentException: publish PaymentResultEvent(orderId, false, "")
    //   Publish results to: "saga-results"
    public void handleCommand(String rawCommand) {
        throw new UnsupportedOperationException("TODO: implement handleCommand()");
    }
}
