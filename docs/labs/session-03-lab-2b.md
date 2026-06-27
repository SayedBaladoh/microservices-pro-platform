# Session 3 — Lab 2B: JWT Authentication Filter & Rate Limiting

**Duration:** 30 min in-session + complete as homework if needed
**Builds on:** Lab 2A (Session 2) — the `api-gateway` module
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Objectives

- Build a custom `JwtAuthFilter` GlobalFilter that validates tokens on every
  protected request
- Whitelist public routes from JWT validation
- Configure `RequestRateLimiter` using Redis as the token bucket store
- Understand `replenishRate` vs `burstCapacity`

## Task 1 — Implement JWT Authentication Filter (15 min)

Add JWT validation to the existing `api-gateway` module:

- `JwtUtil.java` — `validateToken(String token)` returns `Claims`, throws
  `JwtException` if invalid/expired. `isTokenValid(String token)` wraps it
  in a try-catch.
- `JwtAuthFilter.java` — implements `GlobalFilter, Ordered`:
  - `PUBLIC_ROUTES` list (this repo: `/api/v1/products`)
  - Skip validation for public routes
  - Extract and validate the Bearer token
  - Return 401 if missing or invalid
  - Add `X-User-Id` and `X-User-Role` headers from the JWT claims
  - Forward the enriched request to the chain
  - `getOrder()` returns `Ordered.HIGHEST_PRECEDENCE + 1`

You need a real JWT to test this with — see
`docs/labs/session-03-jwt-testing.md` for the `tools/jwt-generator` tool
and full test scenarios (admin token, expired token, tampered token).

## Task 2 — Add Rate Limiting to the Product Route (10 min)

- `RateLimitConfig.java` — an IP-based `KeyResolver` bean (`@Primary`), plus
  a user-based one as a bonus.
- `application.yml` — add `RequestRateLimiter` to the existing
  `product-service` route:
  ```yaml
  redis-rate-limiter.replenishRate: 10
  redis-rate-limiter.burstCapacity: 20
  redis-rate-limiter.requestedTokens: 1
  key-resolver: "#{@ipKeyResolver}"
  ```

Redis must be running:
```bash
docker compose up -d
docker compose ps   # redis should be "running"
```

Test it:
```bash
for i in {1..25}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/products
done
# Expected: ~20 lines of 200, then 429s
```

## Task 3 — Write Unit Tests (5 min)

- `JwtUtilTest.java` (minimum 3, this repo ships 3): valid token → true,
  expired token → false, tampered token → false
- `JwtAuthFilterTest.java` (minimum 2, this repo ships 2): `getOrder()` ==
  `HIGHEST_PRECEDENCE + 1`; `filter()` calls `chain.filter()` for public
  routes

## Acceptance criteria

1. `GET /api/v1/products` (no token) → 200 OK (public route)
2. `POST /api/v1/products` (no token) → 401 Unauthorized with JSON error body
3. `POST /api/v1/products` (valid JWT) → 201 Created (forwarded to Product Service)
4. Send 25 rapid requests → first ~20 return 200, rest return 429
5. `X-RateLimit-Remaining` header visible in responses
6. `X-User-Id` and `X-User-Role` headers visible in downstream service logs
7. All unit tests pass: `mvn test`
8. Commit message: `session-03: add-jwt-auth-filter-and-rate-limiting`

## Homework (recommended)

- Move `PUBLIC_ROUTES` from hardcoded to `application.yml` via
  `@ConfigurationProperties`
- Add expiry validation: generate a token with a 1-minute expiry (`--expiry=1m`
  on `jwt-generator`) and verify you get 401 after it expires
