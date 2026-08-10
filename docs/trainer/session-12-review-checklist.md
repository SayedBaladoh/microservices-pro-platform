# Trainer Review Checklist — Session 12

## Definition of Done

- [ ] `POST /api/orders/orchestrated` → PENDING immediately
- [ ] `GET /api/orders/{id}/status` → COMPLETED on happy path
- [ ] `payment.failure-rate=1.0` → CANCELLED + `[SAGA] COMPENSATING` in logs
- [ ] All 3 `OrderSagaOrchestratorTest` tests pass
- [ ] S7 `createOrder()` endpoint still works (no regressions)
- [ ] `mvn test -pl services/order-service` — all green
- [ ] Commit: `session-12: add-saga-orchestration-with-state-machine`

## Critical checks

- [ ] **10 distinct consumer group IDs (S7 + S12 combined).**
  Run in the repo root:
  ```
  grep -rh "groupId" services/*/src/main/java/ | grep -oP '"[^"]+"' | sort | uniq -d
  ```
  Any output = duplicate found. A duplicated group silently drops messages
  — the hardest class of Kafka bug to diagnose in production.

- [ ] **`sagaStates.get(event.orderId()) != currentExpectedState` guard
  (idempotency) present in all three result handlers.**
  Without this, a duplicate result event (Kafka at-least-once delivery)
  re-triggers an already-completed transition. Ask: "What happens if
  `handlePaymentResult` fires twice for the same orderId?"

- [ ] **`containerFactory = "sagaResultsListenerFactory"` on all three
  `@KafkaListener` methods in `OrderSagaOrchestrator`.**
  Without it, Spring uses the default factory — which may not correctly
  deserialize the typed result events. Symptom: listeners never fire,
  saga stays stuck in `INVENTORY_RESERVING` forever with no error log.

- [ ] **`SagaKafkaConfig` present and `@Configuration` annotated.**
  If missing: "No bean named 'sagaResultsListenerFactory'" at startup.
  This is the most common startup failure in this session.

- [ ] **`InventorySagaCommandHandler` and `PaymentSagaCommandHandler`
  each ignore commands NOT addressed to them.**
  If missing: Inventory handler accidentally handles `ProcessPaymentCommand`
  (parsed as unknown, crashes, publishes no result → saga stalls).
  Ask: "What does your handler do when commandType is unknown?"

- [ ] **S7 Choreography still works.**
  `POST /api/orders` (createOrder) must still return PENDING and the S7
  Saga must still complete. Session 12 adds a NEW entry point
  (`startSaga()`), it does NOT remove or replace the S7 path.
  Quick verification: after implementing S12, run ALL order-service tests
  including the original `OrderSagaTest` (S7) and `OrderServiceTest` (S4/S5).

- [ ] **Technical debt documented, not apologized for.**
  `sagaStates` in-memory is an intentional design decision at this scope,
  explicitly called out in the code. A trainee who "fixed" it by adding
  JPA persistence without being asked may have introduced scope creep.
  Ask them to explain what would be lost/gained by persisting saga state.

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10%.
The "Orchestrator is the Decision Maker" diagram (drawn during the
Architecture discussion, Session 12 §3.3) counts toward Code Quality —
a trainee who implemented it without understanding the design gets
partial credit only.
