package com.microservices.pro.orderservice.saga;

/**
 * SagaState — Session 12.
 *
 * Tracks every step of the Orchestration Saga explicitly.
 * Having all states visible in one enum is itself a key advantage
 * of Orchestration over Choreography: the full flow is readable
 * here without tracing 3 services' @KafkaListener methods.
 *
 * Technical Debt (documented, intentional): this state is held in
 * an in-memory Map<String, SagaState> inside OrderSagaOrchestrator.
 * A service restart loses all in-progress saga state. The homework
 * for Session 12 is to persist this to PostgreSQL (order_sagas table).
 * Not implemented here by design — the teaching goal is the pattern,
 * not the persistence plumbing.
 *
 * State transitions (happy path):
 *   STARTED → INVENTORY_RESERVING → INVENTORY_RESERVED
 *          → PAYMENT_PROCESSING   → PAYMENT_COMPLETED → COMPLETED
 *
 * Compensation path:
 *   PAYMENT_FAILED → COMPENSATING → INVENTORY_RELEASED → CANCELLED
 *
 * Terminal states: COMPLETED, CANCELLED, INVENTORY_FAILED
 */
public enum SagaState {

    STARTED,               // saga initiated, no commands sent yet

    INVENTORY_RESERVING,   // ReserveInventoryCommand sent, awaiting result
    INVENTORY_RESERVED,    // reservation confirmed — triggers ProcessPaymentCommand
    INVENTORY_FAILED,      // reservation failed — no payment attempt (terminal)

    PAYMENT_PROCESSING,    // ProcessPaymentCommand sent, awaiting result
    PAYMENT_COMPLETED,     // payment confirmed (terminal → COMPLETED)
    PAYMENT_FAILED,        // payment failed — triggers ReleaseInventoryCommand

    COMPENSATING,          // ReleaseInventoryCommand sent, awaiting confirmation
    INVENTORY_RELEASED,    // inventory released (terminal → CANCELLED)

    COMPLETED,             // saga ended successfully
    CANCELLED              // saga ended via compensation
}
