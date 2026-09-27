// smoke-test.js — Session 23, Lab 19
//
// The simplest possible k6 script: 1 VU, 10s, just prove the endpoint
// responds correctly before running any serious load.
//
// Run: k6 run k6/smoke-test.js
// Requires: docker compose up (full stack running)

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1,
  duration: '10s',
  thresholds: {
    http_req_duration: ['p(95)<500'],   // 95% of requests under 500ms
    http_req_failed:   ['rate<0.01'],   // less than 1% failure rate
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE_URL}/api/v1/products`);

  check(res, {
    'status is 200':       (r) => r.status === 200,
    'response time < 1s':  (r) => r.timings.duration < 1000,
    'body is JSON array':  (r) => Array.isArray(JSON.parse(r.body)),
  });

  sleep(1);
}
