# Trainer Review Checklist — Session 10

## Definition of Done

- [ ] `ProductControllerTest` — 2 tests, both use `@WebMvcTest` + `@MockBean`
- [ ] `ProductServiceParameterizedTest` — 1 method, 3 `@CsvSource` rows
- [ ] `ProductServiceIntegrationTest` — 2 tests with TestContainers, `static` container + `@DynamicPropertySource`
- [ ] `mvn test -pl services/product-service` — all green
- [ ] Commit: `session-10: add-web-mvc-test-parameterized-and-testcontainers`

## Critical checks

- [ ] **`@Container` is `static`** — if instance-scoped, a new container starts
      for every `@Test` method. Running `ProductServiceIntegrationTest` with
      2 tests means 2 Docker PostgreSQL startups instead of 1. Ask: "How long
      did your tests take? Is there a way to make them faster?"

- [ ] **`@DynamicPropertySource` is `static`** — if not static, Spring cannot
      call it before the context starts. The error is confusing:
      `@DynamicPropertySource must be static`. Check it.

- [ ] **`@WebMvcTest` does NOT load the full context** — a trainee who uses
      `@SpringBootTest` instead "for simplicity" gets a slow 10-20 second test
      instead of a 2 second one. The class name says `@WebMvcTest` specifically.

- [ ] **`@MockBean` is present for `ProductService`** — without it, `@WebMvcTest`
      fails at startup, not at the test. The error message is cryptic:
      "Field productService in ProductController required a bean of type
      ProductService that could not be found." Know what causes it.

- [ ] **`@BeforeEach deleteAll()`** — without this, test 2 sees test 1's data
      and `hasSize(2)` becomes `hasSize(3)` or more. Check that isolation is
      data-level, not context-level (`@DirtiesContext`).

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10%.
