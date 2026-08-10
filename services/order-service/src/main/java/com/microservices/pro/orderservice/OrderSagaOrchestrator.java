package com.microservices.pro.orderservice;

import com.microservices.pro.orderservice.saga.SagaState;
import com.microservices.pro.orderservice.saga.commands.ProcessPaymentCommand;
import com.microservices.pro.orderservice.saga.commands.ReleaseInventoryCommand;
import com.microservices.pro.orderservice.saga.commands.ReserveInventoryCommand;
import com.microservices.pro.orderservice.saga.results.InventoryReleasedEvent;
import com.microservices.pro.orderservice.saga.results.InventoryResultEvent;
import com.microservices.pro.orderservice.saga.results.PaymentResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OrderSagaOrchestrator — Session 12, Lab 10A.
 *
 * The BRAIN of the Orchestration Saga. Plays TWO roles:
 *   ROLE 1 — COMMAND SENDER: uses KafkaTemplate to tell services what TO DO.
 *   ROLE 2 — RESULT LISTENER: uses @KafkaListener to hear what HAPPENED, then
 *             decides the next step.
 *
 * This is the core distinction from Choreography (S7): the Orchestrator
 * DECIDES exclusively. Services are "hands" — they receive a command, execute
 * it, and report back. They do NOT decide what happens next.
 *
 * Topics:
 *   saga-commands  → Orchestrator sends commands TO services
 *   saga-results   → services send results BACK TO Orchestrator
 *
 * This separation from the Choreography topics (order-events, inventory-events,
 * payment-events) is deliberate: commands have different semantics from events.
 * Events announce what HAPPENED. Commands request what SHOULD BE DONE. — Quiz Q6.
 *
 * Technical Debt (documented, intentional):
 *   sagaStates is in-memory (ConcurrentHashMap). A service restart loses all
 *   in-progress saga state. Production: persist to DB (see Session 12 §8 homework).
 *
 * Coexistence with S7 Choreography:
 *   startSaga() is the Orchestration entry point — distinct from createOrder()
 *   (the S7 Choreography entry point). Both coexist in order-service. Neither
 *   replaces the other; the trainer chooses which to demo in production scenarios.
 */
@Service
public class OrderSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

    // DEV ONLY: in-memory state. A service restart loses all in-progress sagas.
    // Production: persist SagaState to PostgreSQL (order_sagas table) and reload
    // on startup. See Session 12 §8 homework for the full spec.
    private final Map<String, SagaState> sagaStates = new ConcurrentHashMap<>();

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    // ── ENTRY POINT ────────────────────────────────────────────────────────────

    /**
     * Starts the Orchestration Saga for a new order.
     *
     * Sequence:
     *   1. Save order as PENDING (local transaction — committed before any Kafka send)
     *   2. Record SagaState.STARTED
     *   3. Send ReserveInventoryCommand to "saga-commands"
     *   4. Transition to INVENTORY_RESERVING
     *   5. Return PENDING response immediately (async from here)
     *
     * Note: the S7 createOrder() also saves a PENDING order and publishes to Kafka,
     * but sends an OrderPlacedEvent (notification) — Choreography services react on
     * their own. Here we send a ReserveInventoryCommand (directive) — the Inventory
     * Command Handler must execute it and report back.
     */
    public OrderResponse startSaga(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();

        Order order = new Order(
                orderId,
                request.productId(),
                request.quantity(),
                request.amount(),
                OrderStatus.PENDING
        );
        orderRepository.save(order);

        sagaStates.put(orderId, SagaState.STARTED);

        kafkaTemplate.send("saga-commands", orderId,
                new ReserveInventoryCommand(orderId, request.productId(), request.quantity()));

        transition(orderId, SagaState.INVENTORY_RESERVING);

        return new OrderResponse(orderId, "PENDING", "Order received — processing...");
    }

    // ── RESULT LISTENERS ────────────────────────────────────────────────────────

    /**
     * Happy path: Inventory confirmed reservation → send payment command.
     * Failure path: Inventory failed → cancel order directly (no compensation needed
     * because nothing was committed beyond the reservation attempt).
     *
     * groupId "orchestrator-inventory" — distinct from all S7 Choreography groups.
     * See session-12-review-checklist.md for the full group-ID uniqueness matrix.
     */
    @KafkaListener(topics = "saga-results", groupId = "orchestrator-inventory",
                   containerFactory = "sagaResultsListenerFactory")
    public void handleInventoryResult(InventoryResultEvent event) {
        SagaState current = sagaStates.get(event.orderId());
        if (current != SagaState.INVENTORY_RESERVING) return; // idempotency guard

        if (event.success()) {
            transition(event.orderId(), SagaState.INVENTORY_RESERVED);

            Order order = orderRepository.findById(event.orderId()).orElseThrow();
            kafkaTemplate.send("saga-commands", event.orderId(),
                    new ProcessPaymentCommand(event.orderId(), order.getAmount(), null));

            transition(event.orderId(), SagaState.PAYMENT_PROCESSING);
        } else {
            transition(event.orderId(), SagaState.INVENTORY_FAILED);
            updateOrderStatus(event.orderId(), OrderStatus.CANCELLED);
            sagaStates.remove(event.orderId());
            log.warn("[SAGA] {} inventory failed: {} — saga cancelled", event.orderId(), event.reason());
        }
    }

    /**
     * Happy path: Payment confirmed → order CONFIRMED, saga COMPLETED.
     * Failure path: Payment failed → send ReleaseInventoryCommand (compensation).
     *
     * groupId "orchestrator-payment" — distinct from orchestrator-inventory.
     */
    @KafkaListener(topics = "saga-results", groupId = "orchestrator-payment",
                   containerFactory = "sagaResultsListenerFactory")
    public void handlePaymentResult(PaymentResultEvent event) {
        SagaState current = sagaStates.get(event.orderId());
        if (current != SagaState.PAYMENT_PROCESSING) return; // idempotency guard

        if (event.success()) {
            transition(event.orderId(), SagaState.PAYMENT_COMPLETED);
            updateOrderStatus(event.orderId(), OrderStatus.CONFIRMED);
            transition(event.orderId(), SagaState.COMPLETED);
            sagaStates.remove(event.orderId());
            log.info("[SAGA] {} COMPLETED — txId: {}", event.orderId(), event.transactionId());
        } else {
            transition(event.orderId(), SagaState.PAYMENT_FAILED);

            Order order = orderRepository.findById(event.orderId()).orElseThrow();
            kafkaTemplate.send("saga-commands", event.orderId(),
                    new ReleaseInventoryCommand(event.orderId(), order.getProductId(), order.getQuantity()));

            transition(event.orderId(), SagaState.COMPENSATING);
            log.warn("[SAGA] {} payment failed — compensation triggered", event.orderId());
        }
    }

    /**
     * Compensation confirmed: Inventory released the reservation → order CANCELLED.
     *
     * groupId "orchestrator-compensation" — distinct from the two above.
     */
    @KafkaListener(topics = "saga-results", groupId = "orchestrator-compensation",
                   containerFactory = "sagaResultsListenerFactory")
    public void handleInventoryReleased(InventoryReleasedEvent event) {
        SagaState current = sagaStates.get(event.orderId());
        if (current != SagaState.COMPENSATING) return; // idempotency guard

        transition(event.orderId(), SagaState.INVENTORY_RELEASED);
        updateOrderStatus(event.orderId(), OrderStatus.CANCELLED);
        transition(event.orderId(), SagaState.CANCELLED);
        sagaStates.remove(event.orderId());
        log.info("[SAGA] {} CANCELLED — inventory released", event.orderId());
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private void transition(String orderId, SagaState newState) {
        SagaState old = sagaStates.put(orderId, newState);
        log.info("[SAGA] {} {} → {}", orderId, old, newState);
    }

    private void updateOrderStatus(String orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }

    /**
     * Package-private test helper — for unit test assertion without reflection.
     * DO NOT call from production code.
     * See Common Issues §9: "Unit test: sagaStates map not accessible for assertion"
     */
    SagaState getSagaState(String orderId) {
        return sagaStates.get(orderId);
    }
}
