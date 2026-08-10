package com.microservices.pro.orderservice;

import com.microservices.pro.orderservice.saga.SagaState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microservices.pro.orderservice.saga.results.InventoryResultEvent;
import com.microservices.pro.orderservice.saga.results.PaymentResultEvent;
import com.microservices.pro.orderservice.saga.results.InventoryReleasedEvent;
import com.microservices.pro.orderservice.saga.commands.ReserveInventoryCommand;
import com.microservices.pro.orderservice.saga.commands.ProcessPaymentCommand;
import com.microservices.pro.orderservice.saga.commands.ReleaseInventoryCommand;

/**
 * OrderSagaOrchestratorTest — Session 12, Lab 10A, Task 3.
 *
 * Unit tests for the Orchestrator — no Kafka broker, no Spring context.
 * Mockito mocks KafkaTemplate and OrderRepository; getSagaState() (package-
 * private) reads the in-memory map to verify state transitions.
 *
 * Test 1: startSaga() → saves PENDING order, sends ReserveInventoryCommand,
 *          state = INVENTORY_RESERVING
 * Test 2: handleInventoryResult(success) + handlePaymentResult(success)
 *          → state progresses to COMPLETED
 * Test 3: handleInventoryResult(success) + handlePaymentResult(failure)
 *          → state transitions to COMPENSATING, ReleaseInventoryCommand sent
 */
@ExtendWith(MockitoExtension.class)
class OrderSagaOrchestratorTest {

    @InjectMocks
    private OrderSagaOrchestrator orchestrator;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    private OrderRequest sampleRequest() {
        return new OrderRequest("PROD-001", 2, new BigDecimal("200.00"), "cust-1");
    }

    // ── Test 1 ─────────────────────────────────────────────────────────────

    @Test
    void startSaga_savesOrder_sendsReserveCommand_transitionsToInventoryReserving() {
        OrderResponse response = orchestrator.startSaga(sampleRequest());

        // Order saved with PENDING status
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);

        // ReserveInventoryCommand sent to "saga-commands"
        verify(kafkaTemplate).send(
                eq("saga-commands"),
                anyString(),
                any(ReserveInventoryCommand.class));

        // Immediate response is PENDING
        assertThat(response.status()).isEqualTo("PENDING");

        // SagaState is INVENTORY_RESERVING
        String orderId = response.orderId();
        assertThat(orchestrator.getSagaState(orderId))
                .isEqualTo(SagaState.INVENTORY_RESERVING);
    }

    // ── Test 2 ─────────────────────────────────────────────────────────────

    @Test
    void handleInventorySuccess_thenPaymentSuccess_sagaReachesCompleted() {
        // Start the saga
        OrderResponse start = orchestrator.startSaga(sampleRequest());
        String orderId = start.orderId();

        // Inventory reserves successfully
        Order order = new Order(orderId, "PROD-001", 2, new BigDecimal("200.00"), OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orchestrator.handleInventoryResult(new InventoryResultEvent(orderId, true, ""));

        assertThat(orchestrator.getSagaState(orderId)).isEqualTo(SagaState.PAYMENT_PROCESSING);
        verify(kafkaTemplate).send(eq("saga-commands"), eq(orderId), any(ProcessPaymentCommand.class));

        // Payment succeeds
        orchestrator.handlePaymentResult(new PaymentResultEvent(orderId, true, "tx-abc"));

        // getSagaState returns null when the saga is removed after COMPLETED
        assertThat(orchestrator.getSagaState(orderId)).isNull();

        // Order status updated to CONFIRMED
        verify(orderRepository).save(org.mockito.ArgumentMatchers.argThat(
                o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    // ── Test 3 ─────────────────────────────────────────────────────────────

    @Test
    void handleInventorySuccess_thenPaymentFailure_sagaTransitionsToCompensating() {
        // Start the saga
        OrderResponse start = orchestrator.startSaga(sampleRequest());
        String orderId = start.orderId();

        // Inventory reserves successfully
        Order order = new Order(orderId, "PROD-001", 2, new BigDecimal("200.00"), OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orchestrator.handleInventoryResult(new InventoryResultEvent(orderId, true, ""));

        // Payment fails
        orchestrator.handlePaymentResult(new PaymentResultEvent(orderId, false, ""));

        // Saga is now COMPENSATING
        assertThat(orchestrator.getSagaState(orderId)).isEqualTo(SagaState.COMPENSATING);

        // ReleaseInventoryCommand was sent
        verify(kafkaTemplate).send(
                eq("saga-commands"),
                eq(orderId),
                any(ReleaseInventoryCommand.class));
    }
}
