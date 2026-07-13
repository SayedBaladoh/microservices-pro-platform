# Trainer Review Checklist — Session 6

Use this when reviewing a trainee's submitted `main` branch for Session 6.

## Definition of Done (from Session 6 docx, Section 8)

- [ ] Inventory Service on port 8084, registered in Eureka
- [ ] Stock check: 200 OK and 409 responses working
- [ ] Order rejects `PROD-003` (no stock)
- [ ] Order confirms `PROD-001` (in stock) — proceeds past the stock check
- [ ] JWT header visible in Inventory Service logs (propagation working)
- [ ] Unit tests passing: `mvn test` green
- [ ] Commit: `session-06: add-inventory-service-and-feign-client`

## Code review points — critical checks first

- [ ] **`inventory-service/pom.xml` has `spring-boot-starter-web`, not a
      WebFlux dependency.** Inventory Service is a plain blocking service.
- [ ] `@EnableFeignClients` is present on `OrderServiceApplication` — its
      absence produces a confusing "no Feign client bean found" error that
      doesn't obviously point back to this annotation.
- [ ] `InventoryErrorDecoder` is registered as a `@Component` and actually
      gets picked up — a trainee who puts it in the wrong package (outside
      component scanning) will see raw `FeignException` instead of the
      domain exceptions, with no obvious error explaining why.
- [ ] `FeignJwtInterceptor` correctly casts
      `RequestContextHolder.getRequestAttributes()` to
      `ServletRequestAttributes` — and null-checks before reading the
      request, since this interceptor is invoked any time a Feign call is
      made, including from contexts where there might not be an inbound
      HTTP request (background jobs, async paths) in later sessions.
- [ ] The stock pre-check happens **before** any Order is saved or any
      payment attempted — a trainee who reorders this (e.g. checks stock
      after saving the order) creates orphaned PENDING orders for rejected
      stock requests.

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md`.

## Common shallow-pass patterns to watch for

- A trainee who hardcodes Inventory Service's host:port in `InventoryClient`
  instead of using `@FeignClient(name="INVENTORY-SERVICE", ...)` —
  technically "works" locally but defeats Eureka discovery entirely.
- Catching `FeignException` broadly in `OrderService` instead of letting
  `InventoryErrorDecoder` translate it to a specific domain exception first
  — masks the actual failure reason from anyone reading the catch block.
