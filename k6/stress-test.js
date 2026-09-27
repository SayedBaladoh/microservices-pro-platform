// stress-test.js — Session 23, Lab 19
//
// Stress test: ramps WELL beyond expected capacity to find the breaking
// point and observe resilience patterns (Bulkhead → TimeLimiter →
// CircuitBreaker) firing in their documented order (Session 5).
//
// Expected findings:
//   1. Bulkhead (max-concurrent-calls=10) saturates first — available-
//      concurrent-calls drops to 0, new requests get QUEUED or rejected.
//   2. TimeLimiter (2s) fires when payment simulation is slow.
//   3. CircuitBreaker opens once failure-rate-threshold (50%) is crossed.
//
// Watch resilience state live in a second terminal while this runs:
//   watch -n1 "curl -s localhost:8082/actuator/bulkheads | jq .details"
//   watch -n1 "curl -s localhost:8082/actuator/circuitbreakers | jq .details"
//
// Correlate k6 error spikes with Zipkin traces at http://localhost:9411.
//
// Run: k6 run -e TEST_JWT=$TEST_JWT k6/stress-test.js

import http   from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate        = new Rate('errors');
const orderLatency     = new Trend('order_latency_ms');

export const options = {
  stages: [
    { duration: '20s', target: 5  },  // warm-up
    { duration: '30s', target: 20 },  // exceed Bulkhead (max=10) → starts rejecting
    { duration: '30s', target: 40 },  // exceed TimeLimiter threshold
    { duration: '30s', target: 60 },  // push Circuit Breaker toward OPEN
    { duration: '20s', target: 0  },  // ramp down — watch CB transition to HALF_OPEN
  ],
  thresholds: {
    // Stress test EXPECTS failures — thresholds here are for RECORDING,
    // not pass/fail. Use --no-thresholds-check flag if you don't want
    // k6 to exit with a non-zero code when resilience patterns kick in.
    http_req_duration: ['p(99)<5000'],  // allow up to 5s P99 under stress
    'errors':          ['rate<0.80'],   // up to 80% errors expected at peak stress
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JWT      = __ENV.TEST_JWT  || '';

const headers = {
  'Content-Type':  'application/json',
  'Authorization': `Bearer ${JWT}`,
};

export default function () {
  const start   = Date.now();

  const payload = JSON.stringify({
    productId:  'PROD-001',
    quantity:   1,
    amount:     100.00,
    customerId: `stress-cust-${__VU}`,
  });

  const res = http.post(`${BASE_URL}/api/orders`, payload, {
    headers,
    timeout: '6s',
  });

  const duration = Date.now() - start;
  orderLatency.add(duration);

  const ok = check(res, {
    'not 5xx server error': (r) => r.status < 500,
    // Note: 429 (Bulkhead/rate-limit) and 503 (CB OPEN) are expected
    // under stress — they count as "ok" here because they are the
    // INTENDED resilience responses, not unexpected server crashes.
    'resilience response or success': (r) =>
      r.status === 200 || r.status === 202 ||
      r.status === 429 || r.status === 503,
  });

  errorRate.add(!ok);
  // No sleep — maximum throughput to stress the system
}
