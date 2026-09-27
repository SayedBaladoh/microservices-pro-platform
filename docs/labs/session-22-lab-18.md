# Session 22 — Lab 18: Outbox Pattern + Idempotency Keys

**Duration:** 15 min in-session (Outbox) + homework
**Services:** order-service (Outbox) + payment-service (Idempotency)
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## Context — Closing Technical Debt

Two items from the Session 8 Architecture Clinic Technical Debt Register:

| Item | Source | Fix |
|---|---|---|
| "Dual-write gap: save() + send() not atomic" | S8 Clinic | Outbox Pattern (Task 1) |
| "No idempotency on payment retry" | S4 design | Idempotency Keys (Task 2) |

## Task 1 — Outbox Pattern in order-service (Lab, 15 min)

### The problem

```java
// Session 7's createOrder() — two operations, not atomic:
Order saved = orderRepository.save(order);        // ① local DB commit
kafkaTemplate.send("order-events", event);        // ② Kafka send
// If JVM crashes between ① and ②: Order saved, event never sent → Saga stalls
```

### The fix

Replace the `kafkaTemplate.send()` in `createOrder()` with an outbox write:

```java
@Transactional   // both saves happen in ONE transaction
public OrderResponse createOrder(OrderRequest request) {
    // ... existing inventory check ...
    Order saved = orderRepository.save(order);

    // Write event to outbox table (same transaction as the Order save)
    String payload = toJson(new OrderPlacedEvent(saved.getId(), ...));
    outboxRepository.save(new OutboxEvent(saved.getId(), "OrderPlaced", payload));

    // NO kafkaTemplate.send() here anymore — OutboxPublisher handles it
    return new OrderResponse(saved.getId(), "PENDING", "Order received");
}
```

`OutboxPublisher` polls `outbox_events` every second and publishes unpublished rows.

Verify:
```bash
# Stop kafka, place an order, restart kafka
# The OutboxPublisher will pick up the row and publish within 1 second
psql -c "SELECT id, aggregate_id, published FROM outbox_events ORDER BY created_at DESC LIMIT 5;"
```

## Task 2 — Idempotency Keys in payment-service (Homework)

Add `Idempotency-Key` header check to `POST /api/v1/payments`:

```java
@PostMapping
public ResponseEntity<PaymentResponse> processPayment(
        @RequestBody PaymentRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String key) {

    if (key != null) {
        var existing = idempotencyRepository.findById(key);
        if (existing.isPresent()) {
            // Return cached response — do NOT charge again
            return ResponseEntity.ok(new PaymentResponse("COMPLETED",
                    existing.get().getResponsePayload()));
        }
        idempotencyRepository.save(new IdempotencyRecord(key, request.orderId()));
    }
    // ... existing payment logic ...
}
```

Test:
```bash
# Same idempotency key, two requests — should get same response both times
curl -X POST localhost:8083/api/v1/payments \
  -H "Idempotency-Key: test-key-001" \
  -d '{"orderId":"order-123","amount":100}'
# Repeat: same key, same response, NO duplicate charge
```

## ⚖ ENGINEERING DECISION — Polling Publisher vs CDC

| | Polling Publisher (implemented) | CDC (e.g. Debezium) |
|---|---|---|
| Latency | ~1s (poll interval) | Near-zero (reads WAL) |
| Infrastructure | PostgreSQL only | + Debezium + Kafka Connect |
| Complexity | Low | High |
| When to use | Most platforms; start here | High-throughput, latency-critical |

## Acceptance criteria

- [ ] `outbox_events` table created (JPA ddl-auto)
- [ ] `createOrder()` writes to outbox, NOT to Kafka directly
- [ ] `OutboxPublisher` publishes within 1s after a restart
- [ ] Duplicate `Idempotency-Key` returns cached response (no double charge)
- [ ] Commit: `session-22: add-outbox-pattern-and-idempotency-keys`
