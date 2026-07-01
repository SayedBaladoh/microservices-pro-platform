# Trainer Review Checklist — Session 5

Use this when reviewing a trainee's submitted `main` branch for Session 5.
Assumes Session 4's checklist already passed — this one focuses on what's
new.

## Definition of Done (from Session 5 docx, Section 8)

- [ ] Bulkhead: 15 concurrent requests → ~5 `QUEUED` visible
- [ ] TimeLimiter: `payment.delay-ms=3000` → `PENDING` in ~2s
- [ ] All 4 annotations present on `createOrderAsync()`
- [ ] `[BULKHEAD]` and `[TIMEOUT]` log prefixes visible
- [ ] Unit tests passing: `mvn test` green
- [ ] Commit: `session-05: add-bulkhead-and-timelimiter-to-order-payment`

## Code review points — critical checks first

- [ ] **Annotation order on `createOrderAsync()` is exactly `@Bulkhead →
      @TimeLimiter → @CircuitBreaker → @Retry`, top to bottom in the
      source.** This is taught explicitly as outermost-to-innermost
      execution order — a trainee who reorders these (even if it happens
      to still compile and mostly work) has missed the point of the
      lesson. Ask them to explain why the order matters, don't just check
      that it's correct.
- [ ] The method signature actually changed to
      `CompletableFuture<OrderResponse>` — a trainee who tries to keep the
      old synchronous `createOrder()` AND bolt `@TimeLimiter` onto it will
      find the annotation silently does nothing (TimeLimiter requires a
      Future to cancel; see Session 5 docx 3.4).
- [ ] `timeoutFallback()`'s return type is `CompletableFuture<OrderResponse>`,
      not the unwrapped `OrderResponse`. This is the single most common
      Session 5 mistake — a mismatched fallback return type causes a
      runtime error that's confusing to a trainee seeing it for the first time.
- [ ] `bulkheadFallback()` catches `BulkheadFullException` specifically,
      not just `Throwable` — using `Throwable` here means a payment
      failure could accidentally trigger the bulkhead fallback message
      ("system busy") instead of the correct CB fallback message, which
      is misleading to an end user.
- [ ] `OrderController` was updated to return
      `CompletableFuture<ResponseEntity<OrderResponse>>` — if a trainee
      left the controller synchronous while making the service method
      async, this won't compile, but double-check they didn't "fix" it by
      calling `.get()` or `.join()` in the controller (which defeats the
      entire purpose of the async wrapper and reintroduces blocking).

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md`.

## Common shallow-pass patterns to watch for

- `max-wait-duration: 0ms` omitted from the Bulkhead config — without it,
  calls queue indefinitely instead of failing fast, and a trainee's "it
  works" demo may mask this because the test traffic isn't sustained
  enough to expose the difference.
- A reflection test that checks the four annotations exist but never
  checks their `name = "paymentService"` value — technically passes with
  four annotations pointed at four different (wrong) instance names.
