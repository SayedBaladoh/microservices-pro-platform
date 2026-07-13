package com.microservices.pro.orderservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderSagaEventHandlerTest — Session 7, Lab 5A, Task 5, Test 3.
 *
 * "handlePaymentFailed() updates order to PAYMENT_FAILED — call the method
 * with a PaymentFailedEvent — verify status update."
 *
 * The docx's own OrderSagaEventHandler.handlePaymentEvent() takes a raw
 * JSON string (see class-level note on OrderSagaEventHandler.java for why),
 * so this test supplies a real JSON payload matching PaymentFailedEvent's
 * fields rather than constructing the event object directly.
 */
@ExtendWith(MockitoExtension.class)
class OrderSagaEventHandlerTest {

    @InjectMocks
    private OrderSagaEventHandler handler;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void handlePaymentEvent_updatesOrderToPaymentFailed_onPaymentFailedJson() {
        Order existingOrder = new Order("order-123", "PROD-001", 2, new BigDecimal("200.00"), OrderStatus.PENDING);
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(existingOrder));

        String paymentFailedJson = "{\"orderId\":\"order-123\",\"reason\":\"PaymentFailed: card declined\"}";

        handler.handlePaymentEvent(paymentFailedJson);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }
}
