package com.microservices.pro.orderservice.saga.commands;

import java.math.BigDecimal;

/**
 * ProcessPaymentCommand — Session 12.
 * Published to: "saga-commands" by OrderSagaOrchestrator.
 * Consumed by: PaymentSagaCommandHandler (filter: commandType == "ProcessPaymentCommand").
 */
public record ProcessPaymentCommand(
        String commandType,
        String orderId,
        BigDecimal amount,
        String customerId
) {
    public ProcessPaymentCommand(String orderId, BigDecimal amount, String customerId) {
        this("ProcessPaymentCommand", orderId, amount, customerId);
    }
}
