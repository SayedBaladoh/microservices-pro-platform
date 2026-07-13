# Trainer Review Checklist — Session 7

Use this when reviewing a trainee's submitted `main` branch for Session 7.

## Definition of Done (from Session 7 docx, Section 8)

- [ ] `POST /api/orders` returns `PENDING` + `orderId`
- [ ] `GET /api/orders/{id}/status` returns `CONFIRMED` (happy path)
- [ ] Compensation: failure rate 100% → order `CANCELLED`
- [ ] All 3 Kafka topics receiving events (check logs)
- [ ] `[SAGA] COMPENSATION` log visible on payment failure
- [ ] Unit tests passing: `mvn test` green
- [ ] Commit: `session-07: add-choreography-saga`

## Code review points — critical checks first

- [ ] **Five distinct consumer group IDs across the three services — verify
      by listing every `@KafkaListener` annotation and its `groupId`
      side-by-side.** This is the single highest-risk mistake in this
      session: a duplicated group ID causes silent message loss (one
      consumer instance "steals" messages meant for another), not a
      compile error, not a startup error, not even a visible runtime error
      — it just looks like "the Saga sometimes doesn't finish" with no
      stack trace to chase. See `docs/labs/session-07-lab-5a.md`'s
      consumer group table; have the trainee paste their actual five
      annotations next to it.
- [ ] **The Session 6 Inventory pre-check (Feign) was NOT removed.** A
      trainee who "cleans up" by deleting the synchronous check because
      "the Saga handles inventory now" has misunderstood the docx's
      explicit instruction that these two mechanisms coexist (sync
      pre-check + async committed reservation are different concerns).
  - [ ] Conversely: check that `createOrderAsync()` (Session 4-5) was
      **not** deleted either, and its five tests still pass unmodified.
      Session 7 adds a new method; it does not replace the old one.
- [ ] `PaymentSagaHandler.handleInventoryReserved()` actually ignores
      non-`InventoryReserved` events on `inventory-events` — a trainee who
      forgets the early-return guard will see Payment attempt to process
      `InventoryReservationFailed` and `InventoryReleased` events too,
      producing nonsensical duplicate Payment activity.
- [ ] `OrderSagaEventHandler` and `InventorySagaHandler` both use
      `@KafkaListener` methods that accept a raw `String` and parse it
      (matching the docx's own simplified approach) — or, if a trainee
      used a typed event parameter directly, confirm
      `spring.json.trusted.packages` is configured correctly for that to
      deserialize without a `ClassCastException`.
- [ ] `releaseStock(orderId)` is idempotent — calling it twice for an
      already-released order should not throw. This is explicitly called
      out in the docx's Common Issues table ("Duplicate compensation").

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md`.

## Common shallow-pass patterns to watch for

- A trainee who tests the happy path manually once and calls it done,
  without setting `payment.failure-rate=1.0` to actually exercise and
  observe the compensation path live.
- Hardcoded topic name strings scattered across multiple files with
  inconsistent casing/spelling (`"order-events"` vs `"Order-Events"`) — ask
  whether a shared constants class would help (not required in this
  scope, but worth the conversation).
