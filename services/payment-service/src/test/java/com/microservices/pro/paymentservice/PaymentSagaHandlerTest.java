package com.microservices.pro.paymentservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PaymentSagaHandlerTest — Session 7.
 *
 * Not explicitly itemized as a numbered test in the docx's Lab 5A Task 5
 * list (which focuses on Order/Inventory side tests), but the docx's own
 * live-coding script is explicit that this handler must "ignore other
 * events on this topic" — that behavior is exercised here since it's easy
 * to silently break (e.g. by removing the early-return guard) without any
 * other test catching it.
 */
@ExtendWith(MockitoExtension.class)
class PaymentSagaHandlerTest {

    @InjectMocks
    private PaymentSagaHandler handler;

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void handleInventoryReserved_ignoresOtherEventTypesOnTheSameTopic() {
        String inventoryReservationFailedJson = "{\"orderId\":\"order-123\",\"reason\":\"out of stock\"}";

        handler.handleInventoryReserved(inventoryReservationFailedJson);

        verify(paymentProcessor, never()).processPayment(org.mockito.ArgumentMatchers.anyString());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void handleInventoryReserved_publishesPaymentCompleted_onSuccess() {
        String inventoryReservedJson = "{\"orderId\":\"order-123\",\"productId\":\"PROD-001\",\"quantity\":2}";
        when(paymentProcessor.processPayment("order-123")).thenReturn("tx-456");

        handler.handleInventoryReserved(inventoryReservedJson);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("order-123"),
                any(com.microservices.pro.paymentservice.events.PaymentCompletedEvent.class));
    }

    @Test
    void handleInventoryReserved_publishesPaymentFailed_whenProcessorThrows() {
        String inventoryReservedJson = "{\"orderId\":\"order-123\",\"productId\":\"PROD-001\",\"quantity\":2}";
        when(paymentProcessor.processPayment("order-123"))
                .thenThrow(new PaymentException("card declined"));

        handler.handleInventoryReserved(inventoryReservedJson);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("order-123"),
                any(com.microservices.pro.paymentservice.events.PaymentFailedEvent.class));
    }
}
