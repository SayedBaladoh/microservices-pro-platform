# Session 5 — Lab 3B: Full Resilience Stack on Order→Payment

**Duration:** 25 min in-session + complete as homework if needed
**Builds on:** Lab 3A (Session 4) — adds Bulkhead and TimeLimiter to the
existing `order-service` code
**Goal:** complete the full Resilience4j stack on the Order→Payment call
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Task 1 — Add Semaphore Bulkhead

```yaml
resilience4j:
  bulkhead:
    instances:
      paymentService:
        max-concurrent-calls: 10    # max 10 simultaneous calls to Payment
        max-wait-duration: 0ms      # fail immediately if full
```

```java
@Bulkhead(name = "paymentService", fallbackMethod = "bulkheadFallback")
// ... above the existing @CircuitBreaker ...

public OrderResponse bulkheadFallback(OrderRequest request, BulkheadFullException ex) {
    return new OrderResponse("QUEUED", "System busy — order queued");
}
```

## Task 2 — Add TimeLimiter with async wrapper

TimeLimiter requires a `CompletableFuture<T>` return type — it needs a
cancellable Future to enforce its timeout. This means the method built in
Session 4 (`createOrder()` returning `OrderResponse` directly) is converted
to `createOrderAsync()` returning `CompletableFuture<OrderResponse>` (this
repo ships the converted version directly, since S4 and S5 build the same
method together — see `docs/labs/session-04-lab-3a.md`).

```yaml
resilience4j:
  timelimiter:
    instances:
      paymentService:
        timeout-duration: 2s
        cancel-running-future: true
```

```java
@Bulkhead(name = "paymentService", fallbackMethod = "bulkheadFallback")
@TimeLimiter(name = "paymentService", fallbackMethod = "timeoutFallback")
@CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
@Retry(name = "paymentService")
public CompletableFuture<OrderResponse> createOrderAsync(OrderRequest request) {
    return CompletableFuture.supplyAsync(() -> { /* ... */ });
}

public CompletableFuture<OrderResponse> timeoutFallback(OrderRequest request, TimeoutException ex) {
    return CompletableFuture.completedFuture(new OrderResponse("PENDING", "Payment timed out — will retry asynchronously"));
}
```

**Annotation order is not arbitrary — it's taught content.** From outermost
to innermost in actual execution: `@Bulkhead → @TimeLimiter →
@CircuitBreaker → @Retry`. Bulkhead is checked first (is there a
concurrent slot?), then TimeLimiter starts its timer, then CircuitBreaker
decides whether to allow the call through, and Retry is innermost.

`OrderController` is updated to return
`CompletableFuture<ResponseEntity<OrderResponse>>` — Spring MVC handles
this transparently; the client still sees a normal synchronous response.

## Task 3 — Unit tests

`OrderServiceTest` (minimum 3, this repo ships 5 — the full S4+S5 merged set):

1. (S4) `paymentFallback()` returns `PENDING` status
2. (S4) Fallback has the correct signature: `(OrderRequest, Throwable)`
3. (S5) `bulkheadFallback()` returns `QUEUED` status, given a `BulkheadFullException`
4. (S5) `timeoutFallback()` returns `PENDING`, wrapped in `CompletableFuture<OrderResponse>`
5. (S5) All four annotations (`@Bulkhead`, `@TimeLimiter`, `@CircuitBreaker`,
   `@Retry`) are present on `createOrderAsync()` — verified via reflection

## Testing manually

```bash
# Send 15 concurrent requests — watch for QUEUED after ~10
for i in {1..15}; do
  curl -s -X POST http://localhost:8082/api/orders \
    -H "Content-Type: application/json" \
    -d '{"amount": 100.00}' &
done
wait
```

```bash
# Set payment.delay-ms=3000 in payment-service's application.yml, restart it,
# then call POST /api/orders — expect a PENDING response in ~2 seconds, not 3.
```

## Acceptance criteria

1. 15 concurrent requests: ~10 return CONFIRMED/fallback, ~5 return QUEUED
2. `payment.delay-ms=3000` → PENDING response arrives in ~2 seconds
3. Actuator shows both CB state AND bulkhead metrics
   (`/actuator/circuitbreakers`, `/actuator/bulkheads`)
4. Logs show `[BULKHEAD]` and `[TIMEOUT]` prefixes for their respective fallbacks
5. Full annotation stack visible on `createOrderAsync()`: `@Bulkhead +
   @TimeLimiter + @CircuitBreaker + @Retry`
6. All unit tests pass: `mvn test`
7. Commit: `session-05: add-bulkhead-and-timelimiter-to-order-payment`

## Homework (recommended)

- Add `management.endpoints.web.exposure.include:
  health,circuitbreakers,bulkheads,retries` if not already present
  (it is, in this repo's shipped `application.yml`)
- Call `/actuator/bulkheads` — observe the `available-concurrent-calls` metric
- Experiment with `max-wait-duration: 500ms` — observe queuing vs immediate rejection
