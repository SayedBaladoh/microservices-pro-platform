# Trainer Review Checklist — Session 4

Use this when reviewing a trainee's submitted `main` branch for Session 4.

## Definition of Done (from Session 4 docx, Section 8)

- [ ] Payment Service running on port 8083 with configurable failure rate
- [ ] Order Service: `@CircuitBreaker` on the resilience-stack method with `paymentFallback()`
- [ ] Order Service: `@Retry` with exponential backoff configured
- [ ] CB state visible in Actuator: `OPEN` after 5+ failures
- [ ] Retry attempts visible in logs with timing gaps
- [ ] All 3 unit tests passing: `mvn test` green
- [ ] Commit: `session-04: add-circuit-breaker-and-retry-on-order-payment`

## Code review points — critical checks first

- [ ] **`order-service/pom.xml` includes `spring-boot-starter-aop`.** This
      is the single most common Session 4 mistake. Without it, the
      `@CircuitBreaker`/`@Retry` annotations are silently ignored — no
      compile error, no runtime error, the method just executes normally
      every time, which is genuinely confusing to debug. Check this before
      anything else.
- [ ] `payment-service/pom.xml` has `spring-boot-starter-web`, NOT a
      WebFlux/reactive dependency — Payment Service is a plain blocking
      service, unlike `api-gateway`. (After the Session 3 `pom.xml`
      mistake with the Gateway, double-check this isn't accidentally
      mirrored in the wrong direction here.)
- [ ] Fallback signature: same parameters as the original method, PLUS the
      exception type as the last parameter. A trainee who writes
      `paymentFallback(OrderRequest request)` with no exception parameter
      will see Resilience4j silently ignore the fallback and rethrow
      instead — flag this immediately, it's a very common mistake.
- [ ] `retry-exceptions` / `ignore-exceptions` lists in `application.yml`
      actually make sense for the trainee's exception types — a trainee
      who lets a payment exception fall outside `retry-exceptions` will
      see CircuitBreaker behavior but never Retry behavior, and won't know
      why.

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md`.

## Common shallow-pass patterns to watch for

- A trainee who sets `failure-rate-threshold` very low (e.g. 10%) "to make
  the demo more reliable" — ask them to explain what this changes about
  production behavior. The point of the lab is understanding the
  threshold, not avoiding flaky demos.
- `RetryLogger` copied verbatim without understanding `RetryRegistry` — ask
  a trainee to explain what `retryRegistry.retry("paymentService")` is
  actually retrieving.
