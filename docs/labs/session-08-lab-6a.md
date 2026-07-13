# Session 8 — Lab 6A: Redis Caching on Product Service

**Duration:** 20 min in-session + complete as homework
**Part:** 6A of 1 — single lab, no Part B
**Service:** Product Service — add a Redis caching layer
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Before you start — a structural note specific to this repository

Session 8's caching pattern (`@Cacheable` / `@CacheEvict` in front of a
repository call) only makes sense in front of a real database query —
caching in front of an in-memory `Map` has no real "DB bottleneck" to
solve. This repository's Product Service was still using the Session 1
in-memory `Map` through Session 7. Before this lab, Session 1's **optional
homework** ("Add JPA + PostgreSQL persistence to Product Service") has
already been completed for you — `Product` is now a JPA `@Entity`, and
`ProductRepository extends JpaRepository<Product, Long>` exists. This was
not new Session 8 content; it was deferred Session 1 homework, done now
because Session 8 genuinely needs it.

**Your actual Session 8 TODOs are only in `ProductService`** — adding the
caching annotations described below.

## Task 1 — Configure Redis Caching (5 min)

Already provided:

- `pom.xml`: `spring-boot-starter-data-redis` + `spring-boot-starter-cache`
- `application.yml`: `spring.cache.type: redis`,
  `spring.cache.redis.time-to-live: 300000` (5 minutes)
- `ProductServiceApplication.java`: `@EnableCaching`

## Task 2 — Apply Caching Annotations (10 min)

In `ProductService.java`:

- `@Cacheable(value="products", key="'all'")` on `findAll()`
- `@Cacheable(value="products", key="#id")` on `findById()`
- `@CacheEvict(value="products", key="#result.id")` on `save()` — **also**
  call `evictAllProductsCache()` to invalidate the cached "all products"
  list. This is a documented trap (Session 8 docx, S08-Q04): evicting only
  the individual product key leaves the cached list stale.
- `@CacheEvict(value="products", key="#id")` on `deleteById()` — also call
  `evictAllProductsCache()`
- `evictAllProductsCache()` itself: `@CacheEvict(value="products", key="'all'")`

> **Note on `#result.id` vs the docx's `#product.id`:** the docx's
> original example annotates an `update(Product product)` method, where
> the id is already known from the parameter. This repo's `ProductService`
> uses a general-purpose `save()` (insert-or-update), where a brand new
> product's id doesn't exist until after the repository call returns — so
> the eviction key must reference the method's *result*, not its
> parameter. Same caching principle, adapted to a slightly different
> method signature.

## Task 3 — Unit Tests (5 min)

`ProductServiceTest` (minimum 3, this repo ships 4 — adapted from Session
1's original tests to mock `ProductRepository` instead of an in-memory Map):

1. `findById()` calls the repository when the cache is cold
2. `save()` calls `repository.save()`
3. `deleteById()` calls `repository.deleteById()`
4. `findAll()` returns an empty list when the repository has nothing

**Note:** this test does NOT exercise `@Cacheable`/`@CacheEvict` behavior
itself — that requires a full Spring context (cache manager, Redis) and is
deferred to Session 10 (Integration Testing), consistent with the
Unit-Tests-only pattern used throughout Sessions 1-8.

## Acceptance criteria

1. First `GET /api/v1/products/1` → log/behavior shows a cache miss (hits the repository)
2. Second `GET /api/v1/products/1` → served from Redis (no repository call)
3. `redis-cli`: `KEYS products*` shows a `products::1` entry
4. `redis-cli`: `TTL products::1` shows remaining seconds (< 300)
5. `PUT`/save on product 1 → cache evicted (both the individual key and the list key)
6. `GET /api/v1/products/1` again → cache miss reappears
7. All unit tests pass: `mvn test`
8. Commit: `session-08: add-redis-caching-to-product-service`

## Part 2 — Architecture Clinic #1 (discussion, no code)

See `docs/trainer/session-08-architecture-clinic-questions.md`. This is a
live discussion exercise — there is no code deliverable for it.

## Homework (recommended)

- Add caching to Inventory Service — cache `GET
  /api/v1/inventory/check?productId=X` with a short TTL (30 seconds).
  Consider: should inventory stock be cached at all, given how frequently
  it changes? Document your reasoning. (Not implemented in this repo.)
- Resolve one technical debt item identified during the Architecture Clinic
