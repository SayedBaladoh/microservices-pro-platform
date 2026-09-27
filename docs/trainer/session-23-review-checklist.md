# Trainer Review Checklist — Session 23

## Definition of Done

- [ ] `k6/smoke-test.js`, `k6/order-load-test.js`, `k6/stress-test.js` present
- [ ] Baseline documented: req/sec, P95, P99, error rate
- [ ] Stress test demonstrates at least one resilience pattern firing
- [ ] Written comparison: actual firing order vs Session 5 theoretical order
- [ ] At least one Zipkin trace correlated to a k6 error
- [ ] Commit: `session-23: add-k6-stress-test-and-bottleneck-findings`

## Critical checks

- [ ] **Smoke test passes before load/stress test.** A trainee who ran the
  stress test against a partially-started stack gets meaningless "100% errors"
  results with nothing to learn from them.

- [ ] **Bulkhead `max-concurrent-calls` is set and the stress test's VU count
  actually exceeds it.** The most common non-result: test runs with 5 VUs
  against a Bulkhead of 10 — patterns never fire because the threshold is
  never crossed. Ask: "What is your configured max-concurrent-calls?"

- [ ] **Written findings include the actual vs predicted firing order.**
  The pedagogical goal of this session is verifying Session 5's theoretical
  execution order (Bulkhead → TimeLimiter → CircuitBreaker → Retry) under
  real load. A trainee who just lists numbers without comparing to the
  prediction has missed the point.

- [ ] **At least one Zipkin trace specifically correlated (not just "I opened
  Zipkin and saw some traces").** The correlation — "k6 reported error at
  timestamp T, I found trace ID X in Zipkin at that time, span Y in service
  Z is where the time was spent" — is the precise root-cause skill this
  session builds.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
