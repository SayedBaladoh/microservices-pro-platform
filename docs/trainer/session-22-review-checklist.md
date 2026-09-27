# Trainer Review Checklist — Session 22

## Definition of Done

- [ ] `OutboxEvent` + `OutboxRepository` + `OutboxPublisher` in order-service
- [ ] `createOrder()` writes to outbox (NOT kafkaTemplate.send() directly)
- [ ] `OutboxPublisher` @Scheduled, publishes and marks as published
- [ ] `IdempotencyRecord` + `PaymentController` idempotency check in payment-service
- [ ] Commit: `session-22: add-outbox-pattern-and-idempotency-keys`

## Critical checks

- [ ] **`@Transactional` spans BOTH the Order save AND the OutboxEvent save.**
  The whole point of the Outbox Pattern is that both writes are in ONE
  atomic transaction. If `OutboxEvent` save is outside the transaction,
  the dual-write gap still exists.

- [ ] **`@EnableScheduling` present on the application class.**
  Without it, `@Scheduled` on `OutboxPublisher` is silently ignored — the
  same "silent AOP requirement" as `@Timed` (S17) and Resilience4j (S4).
  Ask: "How did you verify the OutboxPublisher is actually running?"

- [ ] **`OutboxPublisher` does NOT break the outbox atomicity by calling
  `kafkaTemplate.send()` and `outboxRepository.save()` in separate
  transactions.** Both must be in the same `@Transactional` scope.

- [ ] **`IdempotencyRecord` is looked up BEFORE processing the payment.**
  A trainee who checks AFTER processing has a race condition — two
  concurrent requests with the same key both pass the initial check.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
