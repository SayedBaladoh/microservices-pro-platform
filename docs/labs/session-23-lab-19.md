# Session 23 — Lab 19: Load Testing with k6

**Duration:** 15 min in-session + homework
**Prerequisites:** Full stack running (`docker compose up -d`), TEST_JWT obtained
**Deliverable:** Three k6 scripts + written bottleneck findings
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## Step 1 — Install k6 (if not already done)

```bash
# macOS
brew install k6

# Ubuntu / WSL
sudo gpg -k && sudo gpg --no-default-keyring \
  --keyring /usr/share/keyrings/k6-archive-keyring.gpg \
  --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] \
  https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update && sudo apt-get install k6
```

## Step 2 — Obtain a test JWT

```bash
export TEST_JWT=$(curl -s -X POST \
  "http://localhost:8180/realms/ecommerce-platform/protocol/openid-connect/token" \
  -d "grant_type=client_credentials" \
  -d "client_id=order-service" \
  -d "client_secret=order-service-secret-dev-only" \
  | jq -r .access_token)
echo "JWT obtained (first 20 chars): ${TEST_JWT:0:20}..."
```

## Step 3 — Smoke test (sanity check)

```bash
k6 run k6/smoke-test.js
# Expected: ✓ status is 200, ✓ response time < 1s, ✓ body is JSON array
# If this fails, do NOT proceed to load/stress tests.
```

## Step 4 — Staged load test (baseline)

```bash
k6 run -e TEST_JWT=$TEST_JWT k6/order-load-test.js
# Record: requests/sec, P95 latency, error rate
# This is your BASELINE. Document it in your commit message.
```

## Step 5 — Stress test + observe resilience (Lab deliverable)

Open THREE terminal windows before running:

**Terminal 1:**
```bash
k6 run -e TEST_JWT=$TEST_JWT k6/stress-test.js
```

**Terminal 2 (Bulkhead state):**
```bash
watch -n1 "curl -s localhost:8082/actuator/bulkheads | jq '.details.paymentService'"
```

**Terminal 3 (Circuit Breaker state):**
```bash
watch -n1 "curl -s localhost:8082/actuator/circuitbreakers | jq '.details.paymentService'"
```

**What to record:**
- At what VU count did available-concurrent-calls drop to 0? (Bulkhead)
- At what VU count did Circuit Breaker state change to OPEN?
- What was the firing ORDER? Compare to Session 5's theoretical order.

## Written deliverable (required for commit)

Include in your commit message or a `k6/FINDINGS.md` file:

```
BOTTLENECK FINDINGS — Session 23
Platform baseline (order-load-test.js, 10 VUs, 1 min):
  Requests/sec: ___
  P95 latency:  ___ms
  P99 latency:  ___ms
  Error rate:   ___%

Stress test (stress-test.js):
  Bulkhead fired at approximately ___ VUs (available-concurrent-calls → 0)
  Circuit Breaker opened at approximately ___ VUs
  Firing order observed: ___ → ___ → ___
  Session 5 predicted order:    Bulkhead → TimeLimiter → Circuit Breaker → Retry
  Match? [YES / PARTIAL / NO — explain if not]

One Zipkin trace correlated to a k6 error:
  Trace ID: ___
  Span that showed the failure: ___
  Time in that span: ___ms
```

## Acceptance criteria

- [ ] `k6/smoke-test.js` runs and all checks pass
- [ ] `k6/order-load-test.js` baseline documented (req/sec, P95, P99, error rate)
- [ ] `k6/stress-test.js` observably triggers Bulkhead OR Circuit Breaker
- [ ] Written record: actual firing order vs Session 5 predicted order
- [ ] At least one Zipkin trace correlated to a k6 error
- [ ] Commit: `session-23: add-k6-stress-test-and-bottleneck-findings`
