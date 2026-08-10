package com.microservices.pro.orderservice;

import com.microservices.pro.orderservice.saga.SagaState;
import com.microservices.pro.orderservice.saga.commands.ProcessPaymentCommand;
import com.microservices.pro.orderservice.saga.commands.ReleaseInventoryCommand;
import com.microservices.pro.orderservice.saga.commands.ReserveInventoryCommand;
import com.microservices.pro.orderservice.saga.results.InventoryReleasedEvent;
import com.microservices.pro.orderservice.saga.results.InventoryResultEvent;
import com.microservices.pro.orderservice.saga.results.PaymentResultEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OrderSagaOrchestrator — Session 12, Lab 10A, Task 2.
 *
 * The BRAIN of the Orchestration Saga.
 *   ROLE 1 — COMMAND SENDER  : KafkaTemplate → "saga-commands"
 *   ROLE 2 — RESULT LISTENER : @KafkaListener ← "saga-results"
 *
 * Implement the TODOs below. See docs/labs/session-12-lab-10a.md.
 *
 * Technical Debt (intentional): sagaStates is in-memory — lost on restart.
 */
@Service
public class OrderSagaOrchestrator {

    // DEV ONLY: in-memory. Production: persist to PostgreSQL.
    private final Map<String, SagaState> sagaStates = new ConcurrentHashMap<>();

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    // TODO 1: Implement startSaga(OrderRequest request) → OrderResponse
    //   a. Generate a UUID orderId
    //   b. Save Order with status PENDING via orderRepository.save(...)
    //   c. sagaStates.put(orderId, SagaState.STARTED)
    //   d. kafkaTemplate.send("saga-commands", orderId, new ReserveInventoryCommand(orderId, productId, quantity))
    //   e. sagaStates.put(orderId, SagaState.INVENTORY_RESERVING)
    //   f. return new OrderResponse(orderId, "PENDING", "Order received — processing...")
    public OrderResponse startSaga(OrderRequest request) {
        throw new UnsupportedOperationException("TODO: implement startSaga()");
    }

    // TODO 2: @KafkaListener(topics="saga-results", groupId="orchestrator-inventory",
    //                         containerFactory="sagaResultsListenerFactory")
    //   If success → send ProcessPaymentCommand, transition to PAYMENT_PROCESSING
    //   If failure → cancel order, state = INVENTORY_FAILED
    public void handleInventoryResult(InventoryResultEvent event) {
        throw new UnsupportedOperationException("TODO: implement handleInventoryResult()");
    }

    // TODO 3: @KafkaListener(topics="saga-results", groupId="orchestrator-payment",
    //                         containerFactory="sagaResultsListenerFactory")
    //   If success → CONFIRMED, state = COMPLETED
    //   If failure → send ReleaseInventoryCommand, state = COMPENSATING
    public void handlePaymentResult(PaymentResultEvent event) {
        throw new UnsupportedOperationException("TODO: implement handlePaymentResult()");
    }

    // TODO 4: @KafkaListener(topics="saga-results", groupId="orchestrator-compensation",
    //                         containerFactory="sagaResultsListenerFactory")
    //   Update order to CANCELLED, state = CANCELLED
    public void handleInventoryReleased(InventoryReleasedEvent event) {
        throw new UnsupportedOperationException("TODO: implement handleInventoryReleased()");
    }

    /** Package-private — for unit test assertion only. DO NOT use in production code. */
    SagaState getSagaState(String orderId) {
        return sagaStates.get(orderId);
    }
}
