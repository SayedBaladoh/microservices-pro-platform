# Trainer Review Checklist — Session 2

Use this when reviewing a trainee's submitted `main` branch for Session 2.

## Definition of Done (from Session 2 docx, Section 8)

- [ ] `api-gateway` module present under `infrastructure/api-gateway`
- [ ] `application.yml` has the `product-service` route using `lb://` URI
- [ ] `LoggingFilter` implemented and logging to console
- [ ] `X-Platform` header visible in a Postman/curl response
- [ ] `API-GATEWAY` registered in the Eureka dashboard
- [ ] All unit tests passing (`mvn test` = green)
- [ ] Commit message format: `session-02: ...`

## Code review points — critical check first

- [ ] **`pom.xml` does NOT contain `spring-boot-starter-web`.** This is the
      single most common Session 2 mistake — if present, the module will
      fail to start (WebFlux/Servlet conflict). Reject the submission until
      fixed; don't just deduct points, since the service literally won't run.
- [ ] `LoggingFilter` implements both `GlobalFilter` and `Ordered`
- [ ] `getOrder()` returns `Ordered.HIGHEST_PRECEDENCE` (not a magic number)
- [ ] The route's `Path` predicate matches the trainee's actual Product
      Service controller path — if they changed `/api/v1/products` in
      Session 1, the Session 2 route must match or the route will 404
- [ ] `StripPrefix` value makes sense for their path structure (this repo
      uses `StripPrefix=0` since Gateway path == service path)

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md` for the full rubric.

## Common shallow-pass patterns to watch for

- A `LoggingFilter` test that mocks `chain.filter()` to return `Mono.empty()`
  but never verifies it was actually called — the test passes even if the
  filter eats the request and never forwards it
- Hardcoding `PRODUCT-SERVICE`'s actual host:port in the route instead of
  using `lb://PRODUCT-SERVICE` (defeats the whole point of Eureka integration)
