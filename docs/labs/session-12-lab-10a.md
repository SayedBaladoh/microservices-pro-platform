# Session 12 — Lab 10A: Saga Orchestration with State Machine

**Duration:** 2.5 hours online
**Services:** order-service (Orchestrator) + inventory-service + payment-service
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Context — Choreography vs Orchestration

| | Choreography (S7) | Orchestration (S12) |
|---|---|---|
| Decision maker | Each service decides its own next step | One Orchestrator decides everything |
| Visibility | Distributed across 3 service logs | Single log in Orchestrator |
| Coupling | Services know the domain flow | Services are "hands" — they know nothing about each other |
| Debuggability | Hard — trace events across services | Easy — SagaState shows exactly where you are |

Both patterns coexist in this repo. `createOrder()` (S7 Choreography) and
`startSaga()` (S12 Orchestration) are separate entry points. Neither replaces the other.

## Task 1 — Command & Result handlers in each service (30 min)

**inventory-service — `InventorySagaCommandHandler`:**

```java
@KafkaListener(topics = "saga-commands", groupId = "inventory-saga-handler")
public void handleCommand(String rawCommand) {
    Map<String, Object> cmd = objectMapper.readValue(rawCommand, Map.class);
    String commandType = (String) cmd.getOrDefault("commandType", "");
    switch (commandType) {
        case "ReserveInventoryCommand" -> handleReserve(cmd);
        case "ReleaseInventoryCommand" -> handleRelease(cmd);
        default -> { /* ignore — not our command */ }
    }
}
```

On successful reserve, publish to `"saga-results"`:
```java
kafkaTemplate.send("saga-results", orderId,
    Map.of("type","InventoryResultEvent","orderId",orderId,"success",true,"reason",""));
```

On `InsufficientStockException`, publish with `"success": false`.

**payment-service — `PaymentSagaCommandHandler`:**

Only handles `commandType == "ProcessPaymentCommand"`. All other commands → return immediately.

On success: `PaymentResultEvent(orderId, true, transactionId)`
On `PaymentException`: `PaymentResultEvent(orderId, false, "")`

## Task 2 — OrderSagaOrchestrator (60 min)

Implement the four TODOs in `OrderSagaOrchestrator.java`:

**TODO 1 — `startSaga()`:**
```java
String orderId = UUID.randomUUID().toString();
orderRepository.save(new Order(orderId, productId, quantity, amount, OrderStatus.PENDING));
sagaStates.put(orderId, SagaState.STARTED);
kafkaTemplate.send("saga-commands", orderId,
    new ReserveInventoryCommand(orderId, productId, quantity));
sagaStates.put(orderId, SagaState.INVENTORY_RESERVING);
return new OrderResponse(orderId, "PENDING", "Order received — processing...");
```

**TODO 2 — `handleInventoryResult()` with `@KafkaListener`:**
```java
@KafkaListener(topics="saga-results", groupId="orchestrator-inventory",
               containerFactory="sagaResultsListenerFactory")
public void handleInventoryResult(InventoryResultEvent event) {
    if (sagaStates.get(event.orderId()) != SagaState.INVENTORY_RESERVING) return;
    if (event.success()) {
        sagaStates.put(event.orderId(), SagaState.INVENTORY_RESERVED);
        Order order = orderRepository.findById(event.orderId()).orElseThrow();
        kafkaTemplate.send("saga-commands", event.orderId(),
            new ProcessPaymentCommand(event.orderId(), order.getAmount(), null));
        sagaStates.put(event.orderId(), SagaState.PAYMENT_PROCESSING);
    } else {
        sagaStates.put(event.orderId(), SagaState.INVENTORY_FAILED);
        updateOrderStatus(event.orderId(), OrderStatus.CANCELLED);
        sagaStates.remove(event.orderId());
    }
}
```

**TODO 3 — `handlePaymentResult()`:** Happy path → CONFIRMED + COMPLETED. Failure path → send `ReleaseInventoryCommand` + COMPENSATING.

**TODO 4 — `handleInventoryReleased()`:** CANCELLED + remove from map.

## Task 3 — Unit Tests (30 min)

`OrderSagaOrchestratorTest` (3 tests, already provided — make them pass):

1. `startSaga()` → saves PENDING order + sends `ReserveInventoryCommand` + state = `INVENTORY_RESERVING`
2. Inventory success → Payment success → state removed (COMPLETED)
3. Inventory success → Payment failure → `ReleaseInventoryCommand` sent + state = `COMPENSATING`

## Consumer Group Checklist (⚠️ verify before testing)

Total 10 distinct group IDs across S7 + S12 — NO duplicates allowed:

| Group ID | Session | Topic | Handler |
|---|---|---|---|
| order-service | S7 | payment-events | OrderSagaEventHandler.handlePaymentEvent |
| order-service-cancel | S7 | inventory-events | OrderSagaEventHandler.handleInventoryReleased |
| inventory-service | S7 | order-events | InventorySagaHandler.handleOrderPlaced |
| inventory-compensation | S7 | payment-events | InventorySagaHandler.handlePaymentFailed |
| payment-service | S7 | inventory-events | PaymentSagaHandler.handleInventoryReserved |
| orchestrator-inventory | S12 | saga-results | OrderSagaOrchestrator.handleInventoryResult |
| orchestrator-payment | S12 | saga-results | OrderSagaOrchestrator.handlePaymentResult |
| orchestrator-compensation | S12 | saga-results | OrderSagaOrchestrator.handleInventoryReleased |
| inventory-saga-handler | S12 | saga-commands | InventorySagaCommandHandler.handleCommand |
| payment-saga-handler | S12 | saga-commands | PaymentSagaCommandHandler.handleCommand |

## Acceptance criteria

- [ ] `POST /api/orders/orchestrated` → status PENDING immediately
- [ ] `GET /api/orders/{id}/status` → transitions: INVENTORY_RESERVING → INVENTORY_RESERVED → PAYMENT_PROCESSING → COMPLETED
- [ ] Set `payment.failure-rate=1.0` → status = CANCELLED, `[SAGA] COMPENSATING` in logs
- [ ] All 3 `OrderSagaOrchestratorTest` tests pass
- [ ] S7 Choreography still works: `POST /api/orders` (original endpoint) unaffected
- [ ] `mvn test -pl services/order-service` — no regressions
- [ ] Commit: `session-12: add-saga-orchestration-with-state-machine`

## Technical Debt Register (add to Architecture Clinic list)

- `sagaStates` in-memory → lost on restart. Homework: persist to `order_sagas` table.
- `commandType` routing uses raw JSON Map → fragile. Production: use Spring Kafka type headers.
- No timeout on saga steps → if Inventory never responds, Orchestrator waits forever.
