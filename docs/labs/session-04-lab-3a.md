# Session 4 — Lab 3A: Circuit Breaker & Retry on Payment Service

**Duration:** 20 min in-session + complete as homework if needed
**Part:** 3A of 2 — Part 3B in Session 5 (Bulkhead + TimeLimiter)
**Services:** `services/order-service` (caller) + `services/payment-service` (callee with failures)
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Context — what you're building

Order Service calls Payment Service. Payment Service is unstable — it fails
randomly (`payment.failure-rate`, default 50%). Your job: make Order Service
resilient.

- **Circuit Breaker:** stop hammering Payment when it's clearly broken
- **Retry:** try again for transient failures before giving up
- **Fallback:** return a PENDING order when payment can't be processed

Platform flow: `Client → Gateway → Order Service → Payment Service`

> **Scope note:** in this repo, `OrderService` calls a `PaymentService` bean
> defined *inside* `order-service` itself — not the real
> `services/payment-service` module over HTTP. Real inter-service HTTP
> calls (OpenFeign) are Session 6 content; wiring that now would be
> curriculum leakage. See the comment at the top of
> `order-service/.../PaymentService.java` for the full rationale.

## Task 1 — Payment Service with controlled failures

Already provided in `services/payment-service`:

- `POST /api/payments` endpoint
- `payment.failure-rate` configurable (default 0.5 = 50%)

## Task 2 — Apply @CircuitBreaker on Order Service

In `services/order-service`:

- `OrderService.createOrderAsync()` (see note: this repo ships the
  Session-5-merged async version from the start, since Sessions 4 and 5
  build the same method together — see `docs/labs/session-05-lab-3b.md`
  for why) is annotated `@CircuitBreaker(name="paymentService",
  fallbackMethod="paymentFallback")`
- `paymentFallback(OrderRequest request, Throwable ex)` returns an
  `OrderResponse` with status `"PENDING"`
- Configured in `application.yml`:
  ```yaml
  resilience4j:
    circuitbreaker:
      instances:
        paymentService:
          sliding-window-size: 10
          failure-rate-threshold: 50
          wait-duration-in-open-state: 5s
  ```

## Task 3 — Add Retry with exponential backoff

```yaml
resilience4j:
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
```

`RetryLogger` (bonus, included in this repo) logs each attempt with a
`[RETRY]` prefix so you can see the 500ms → 1000ms → 2000ms gaps live.

## Task 4 — Unit tests

`OrderServiceTest` (minimum 3, this repo ships more — see
`docs/labs/session-05-lab-3b.md` for the full merged list):

1. `createOrder()` returns `CONFIRMED` when payment succeeds
2. `paymentFallback()` returns `PENDING` status
3. Fallback has the correct signature: `(OrderRequest, Throwable)`

## Acceptance criteria

1. `POST /api/orders` → returns `"CONFIRMED"` when payment succeeds
2. `POST /api/orders` → returns `"PENDING"` when payment fails consistently
3. After 5+ payment failures: `GET /actuator/circuitbreakers` shows `state: OPEN`
4. After 5s wait: state transitions to `HALF_OPEN` automatically
5. Retry attempts visible in logs: `[RETRY] Attempt #1`, `#2`, `#3`
6. Exponential timing visible: 500ms → 1000ms between retry attempts
7. All unit tests pass: `mvn test`
8. Commit: `session-04: add-circuit-breaker-and-retry-on-order-payment`

## Homework (recommended)

- **Add Payment Service route to API Gateway:** `Route: /api/payments/** →
  lb://PAYMENT-SERVICE`. This is documented homework, not part of this
  lab's required scope — `infrastructure/api-gateway/application.yml` is
  not modified by this lab.
- Test the full flow once you've added the route: Client → Gateway (JWT
  check) → Order Service → Circuit Breaker → Payment
- Observe what happens to CB state when `payment.failure-rate=0.0`
