# Session 18 — Lab 13B: CQRS — Command/Query Split in product-service

**Duration:** 25 min in-session + homework
**Service:** product-service
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## 🎯 DESIGN CHOICE — When to apply CQRS

| Option | When to use |
|---|---|
| No separation | CRUD app, simple reads, team < 5, no separate scaling needs |
| Projection only | Reads need a different shape (join, flatten) but write logic is simple |
| Full CQRS (today) | Write model has complex invariants AND reads need a clearly different shape |
| Full CQRS + separate DB | Very high read/write ratio, read model can tolerate eventual consistency |

**Out of scope today:** Event Sourcing, Axon Framework, separate read database.

## Task 1 — ProductCommandService (10 min)

Create `ProductCommandService.java`. It owns all writes:

```java
@Service
public class ProductCommandService {
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Product createProduct(String name, String description,
                                  BigDecimal price, String category) {
        // 1. Validate invariants (price > 0, name not blank)
        // 2. productRepository.save(...)
        // 3. eventPublisher.publishEvent(new ProductChangedEvent(id, "CREATED"))
        // 4. return saved product
    }
}
```

**Invariants live ONLY on the Command side.** Queries read already-valid state.

## Task 2 — ProductQueryService + Projection (10 min)

Create `ProductSummaryProjection` (interface):
```java
public interface ProductSummaryProjection {
    Long getId();
    String getName();
    String getCategory();
    BigDecimal getPrice();
}
```

Add to `ProductRepository`:
```java
@Query("SELECT p.id as id, p.name as name, p.category as category, p.price as price FROM Product p")
List<ProductSummaryProjection> findAllSummaries();

@Query("SELECT p.id as id, p.name as name, p.category as category, p.price as price FROM Product p WHERE p.id = :id")
Optional<ProductSummaryProjection> findSummaryById(@Param("id") Long id);
```

Create `ProductQueryService`:
```java
@Service
public class ProductQueryService {
    @Transactional(readOnly = true)  // on the service method, not just the repo
    @Cacheable(value = "products", key = "'all-summary'")
    public List<ProductSummaryProjection> findAllSummaries() { ... }
}
```

## Task 3 — ApplicationEvent + Listener (5 min)

`ProductChangedEvent` (record — already provided):
```java
public record ProductChangedEvent(Long productId, String changeType) {}
```

Create `ProductCacheEvictionListener`:
```java
@Component
public class ProductCacheEvictionListener {
    @EventListener
    public void onProductChanged(ProductChangedEvent event) {
        // Evict BOTH keys — missing one leaves stale data
        cache.evict(event.productId());
        cache.evict("all-summary");
    }
}
```

## Task 4 — Unit Tests (in-class checkpoint)

`ProductCommandServiceTest` (2 tests — provided as skeleton):
1. `createProduct()` saves product AND publishes `ProductChangedEvent("CREATED")`
2. `createProduct()` with price ≤ 0 throws `IllegalArgumentException`

Run: `mvn test -pl services/product-service -Dtest=ProductCommandServiceTest`

## Acceptance criteria

- [ ] `ProductCommandService` owns all writes + publishes event
- [ ] `ProductQueryService` returns `ProductSummaryProjection`, `readOnly=true`
- [ ] `ProductCacheEvictionListener` evicts BOTH cache keys
- [ ] `GET /api/v1/products` returns projection shape (not full entity)
- [ ] After a `POST /api/v1/products`, cache is evicted and next GET is a cache miss
- [ ] 2 unit tests passing: `mvn test` green
- [ ] Commit: `session-18: add-cqrs-command-query-split-product-service`
