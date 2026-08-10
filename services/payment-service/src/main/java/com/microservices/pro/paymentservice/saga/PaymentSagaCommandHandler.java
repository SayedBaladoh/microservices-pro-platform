package com.microservices.pro.paymentservice.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.pro.paymentservice.PaymentException;
import com.microservices.pro.paymentservice.PaymentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PaymentSagaCommandHandler — Session 12.
 *
 * Payment Service's "hand" in the Orchestration Saga. Receives
 * ProcessPaymentCommand from the Orchestrator via "saga-commands",
 * executes payment via PaymentProcessor, and publishes result to
 * "saga-results".
 *
 * Consumer group: "payment-saga-handler"
 *   Distinct from ALL existing groups:
 *     payment-service         (S7 — handles inventory-events, Choreography)
 *     inventory-saga-handler  (S12 — Inventory handler)
 *     orchestrator-*          (S12 — Orchestrator listeners)
 *
 * Command routing: filters on commandType == "ProcessPaymentCommand".
 * All other commands (ReserveInventoryCommand, ReleaseInventoryCommand)
 * are ignored — they are for the Inventory handler.
 */
@Service
public class PaymentSagaCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaCommandHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PaymentProcessor paymentProcessor;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "saga-commands", groupId = "payment-saga-handler")
    public void handleCommand(String rawCommand) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cmd = objectMapper.readValue(rawCommand, Map.class);
            String commandType = (String) cmd.getOrDefault("commandType", "");

            if (!"ProcessPaymentCommand".equals(commandType)) {
                log.debug("[PAYMENT-HANDLER] Ignoring command of type: {}", commandType);
                return;
            }

            String orderId = (String) cmd.get("orderId");
            // amount is parsed but not used by the randomised PaymentProcessor —
            // it would be used by a real payment gateway integration
            log.info("[PAYMENT-HANDLER] Processing payment for order {}", orderId);

            try {
                String transactionId = paymentProcessor.processPayment(orderId);

                kafkaTemplate.send("saga-results", orderId,
                        Map.of("type", "PaymentResultEvent",
                               "orderId", orderId,
                               "success", true,
                               "transactionId", transactionId));

                log.info("[PAYMENT-HANDLER] Payment completed for order {} txId={}", orderId, transactionId);
            } catch (PaymentException e) {
                kafkaTemplate.send("saga-results", orderId,
                        Map.of("type", "PaymentResultEvent",
                               "orderId", orderId,
                               "success", false,
                               "transactionId", ""));

                log.warn("[PAYMENT-HANDLER] Payment failed for order {}: {}", orderId, e.getMessage());
            }

        } catch (Exception e) {
            log.error("[PAYMENT-HANDLER] Failed to parse command: {}", rawCommand, e);
        }
    }
}
