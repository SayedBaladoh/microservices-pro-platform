# Trainer Review Checklist — Session 3

Use this when reviewing a trainee's submitted `main` branch for Session 3.

## Definition of Done (from Session 3 docx, Section 8)

- [ ] `JwtUtil` implemented and tested (3 unit tests passing)
- [ ] `JwtAuthFilter`: 401 for missing token, 200/201 for valid token
- [ ] `GET /api/v1/products` (no token) still returns 200 (public route)
- [ ] Rate limiter active: 429 visible after burst capacity exceeded
- [ ] `X-RateLimit-Remaining` header visible in responses
- [ ] All 5 unit tests passing: `mvn test` green
- [ ] Commit: `session-03: add-jwt-auth-filter-and-rate-limiting`

## Code review points — critical checks first

- [ ] **`JwtAuthFilter.getOrder()` returns `HIGHEST_PRECEDENCE + 1`, not
      `HIGHEST_PRECEDENCE` itself.** If it's the same value as
      `LoggingFilter`, ordering between the two is undefined — rejected
      requests might not get logged.
- [ ] The fallback secret in the trainee's `application.yml` matches the one
      they used to generate test tokens. A mismatched secret produces 401 on
      every single valid token, which trainees often misdiagnose as a bug in
      their filter logic instead of a config mismatch — ask first.
- [ ] `PUBLIC_ROUTES` whitelist uses `path::startsWith`, not `path::equals` —
      otherwise `/api/v1/products/123` (single-product GET, if they added
      one) would incorrectly require a token while the list route doesn't.
- [ ] Rate limiter `key-resolver` bean reference (`#{@ipKeyResolver}`)
      actually matches a `@Bean` method name in `RateLimitConfig` — a typo
      here fails silently at startup with a confusing `NoKeyResolver`-style
      error.
- [ ] No trainee added a login/auth endpoint of their own to "make testing
      easier" — flag this immediately. Token issuance is Session 20 scope;
      `tools/jwt-generator` exists specifically so nobody needs to build this
      early. See the Training Note in that tool's README if a trainee pushes
      back.

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md`.

## Common shallow-pass patterns to watch for

- A `JwtAuthFilterTest` that only checks `getOrder()` and never actually
  exercises `filter()` — the "minimum 2 tests" requirement is trivially
  satisfiable with two weak tests; check that at least one test actually
  calls `filter()` and verifies behavior, not just configuration.
- Rate limiting tested manually once during the lab and never automated —
  that's expected and fine (no live-Redis unit tests are required), but
  confirm the trainee can explain *why* it isn't unit-tested (Redis is an
  external dependency; this belongs to integration testing, taught properly
  in Session 10).
