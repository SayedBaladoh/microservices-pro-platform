# Trainer Review Checklist — Session 1

Use this when reviewing a trainee's submitted `main` branch for Session 1.

## Definition of Done (from Session 1 docx, Section 8)

- [ ] Config Server running and serving config (tested with curl)
- [ ] Eureka Server running — dashboard accessible at localhost:8761
- [ ] Product Service registered in Eureka dashboard
- [ ] All 4 REST endpoints working: `GET` all, `GET` by id, `POST`, `DELETE`
- [ ] Minimum 3 unit tests — all passing (`mvn test` = green)
- [ ] Code committed and pushed with format `session-01: ...`

## Code review points

- [ ] `ProductService` uses an in-memory store (no premature JPA unless they
      did the optional homework — if they did, check the homework rubric instead)
- [ ] `ProductController` returns 404 (not 200 with null body) for a missing id
- [ ] `ProductController` returns 201 (not 200) on create
- [ ] No hardcoded Eureka/Config Server URLs scattered in Java code — they
      should be in `application.yml` only
- [ ] Tests actually assert something (not just "doesn't throw")

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md` for the full rubric.

## Common shallow-pass patterns to watch for

- `findAll()` returning a mutable reference to the internal map (encapsulation leak)
- Tests that mock `ProductService` itself instead of testing it directly —
  this lab asks for service-layer tests, not controller-layer tests
- A `save()` that doesn't handle the "id already provided" case sanely
