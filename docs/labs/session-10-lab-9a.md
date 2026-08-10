# Session 10 — Lab 9A: Unit Testing + Integration Testing

**Duration:** 2.5 hours online
**Services:** `services/product-service` (primary)
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Context — the Testing Pyramid

```
        ▲  E2E Tests      ← slow, expensive, few
       ▲▲  Integration    ← medium (TestContainers)
      ▲▲▲  Unit Tests     ← fast, many, the base
```

This lab works from the base upward: unit tests with `@WebMvcTest` and
`@ParameterizedTest` (Tasks 1-2), then integration tests with
TestContainers (Task 3).

## Task 1 — @WebMvcTest: Controller Layer (45 min)

Open `ProductControllerTest.java`. The skeleton has `@WebMvcTest(ProductController.class)`
and a `@MockBean ProductService` field already declared.

Implement the two tests:

**Test 1: `getProduct_returns200_andBody_whenProductExists`**
```java
when(productService.findById(1L))
    .thenReturn(Optional.of(new Product(1L, "Laptop Pro", "desc",
                             new BigDecimal("1299.99"), "ELECTRONICS")));

mockMvc.perform(get("/api/v1/products/1").accept(MediaType.APPLICATION_JSON))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.name").value("Laptop Pro"))
       .andExpect(jsonPath("$.price").value(1299.99));
```

**Test 2: `getProduct_returns404_whenNotFound`**
```java
when(productService.findById(99L)).thenReturn(Optional.empty());

mockMvc.perform(get("/api/v1/products/99"))
       .andExpect(status().isNotFound());
```

**Why `@WebMvcTest` and not `@SpringBootTest`?**

| | `@WebMvcTest` | `@SpringBootTest` |
|---|---|---|
| Context size | Web layer only | Full application |
| Speed | ~2s | ~10-20s |
| Use for | HTTP request/response | End-to-end integration |
| Beans available | Controllers only | Everything |

`@MockBean` is REQUIRED with `@WebMvcTest` — ProductService is not loaded,
so without it the context fails to start.

## Task 2 — @ParameterizedTest Refactor (30 min)

Open `ProductServiceParameterizedTest.java`. Replace the `throw` with a
real implementation:

```java
@ParameterizedTest(name = "findById({0}) shouldExist={2}")
@CsvSource({
    "1, Laptop Pro, true",
    "2, Wireless Mouse, true",
    "999, N/A, false"
})
void findById_returnsCorrectResult(Long id, String name, boolean shouldExist) {
    if (shouldExist) {
        Product p = new Product(id, name, "desc", new BigDecimal("99.99"), "ELECTRONICS");
        when(productRepository.findById(id)).thenReturn(Optional.of(p));
    } else {
        when(productRepository.findById(id)).thenReturn(Optional.empty());
    }
    Optional<Product> result = productService.findById(id);
    assertThat(result.isPresent()).isEqualTo(shouldExist);
    if (shouldExist) assertThat(result.get().getName()).isEqualTo(name);
}
```

Run: `mvn test -pl services/product-service -Dtest=ProductServiceParameterizedTest`

JUnit shows: `findById(1) shouldExist=true`, `findById(2) shouldExist=true`,
`findById(999) shouldExist=false` — three runs from one method.

## Task 3 — TestContainers Integration Test (45 min)

Open `ProductServiceIntegrationTest.java`. Implement the five TODOs:

**TODO 2 — @DynamicPropertySource:**
```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url",      postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.cache.type", () -> "none");
    registry.add("spring.data.redis.repositories.enabled", () -> "false");
}
```

**TODO 3 — @BeforeEach isolation:**
```java
@BeforeEach
void clearDatabase() {
    productRepository.deleteAll();
}
```

**Why not `@DirtiesContext`?**
`@DirtiesContext` restarts the Spring context between tests (slow).
`deleteAll()` + TestContainers gives isolation at the data level, not the
context level. One container, one context, N test methods — fast.

**TODO 4 — Test 1:**
```java
Product saved = productService.save(
    new Product(null, "Laptop Pro", "High-end", new BigDecimal("1299.99"), "ELECTRONICS"));
assertThat(saved.getId()).isNotNull();
Optional<Product> found = productService.findById(saved.getId());
assertThat(found).isPresent();
assertThat(found.get().getName()).isEqualTo("Laptop Pro");
```

**TODO 5 — Test 2:**
```java
productService.save(new Product(null, "Mouse", "Wireless", new BigDecimal("29.99"), "ACC"));
productService.save(new Product(null, "Keyboard", "Mech", new BigDecimal("89.99"), "ACC"));
List<Product> all = productService.findAll();
assertThat(all).hasSize(2);
```

Run: `mvn test -pl services/product-service -Dtest=ProductServiceIntegrationTest`

Requires Docker Desktop. If Docker is not running, TestContainers prints
a warning and skips the test (does not fail the build).

## Acceptance criteria

- [ ] `ProductControllerTest`: both tests pass (green)
- [ ] `ProductServiceParameterizedTest`: 3 rows × 1 method = 3 test runs, all green
- [ ] `ProductServiceIntegrationTest`: 2 tests pass against real PostgreSQL container
- [ ] `mvn test -pl services/product-service` — all tests green
- [ ] Commit: `session-10: add-web-mvc-test-parameterized-and-testcontainers`

## Common pitfalls

See `docs/setup/troubleshooting.md` — Session 10 section.
