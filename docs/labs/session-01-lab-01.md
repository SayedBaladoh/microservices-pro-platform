# Session 1 — Lab 1: Product Service + Eureka + Config

**Duration:** 50 minutes (in-session) + finish as homework if needed
**Repository path:** `services/product-service/`
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Objectives

- Build a Product REST API with Spring Boot 3.x (JDK 21)
- Connect the service to the Config Server for externalized configuration
- Register the service with Eureka Discovery Server
- Write unit tests covering the service layer (minimum 3 test methods)
- Run the full local stack and verify everything works

## Prerequisites

- JDK 21, Docker Desktop, Git — see `docs/setup/local-setup.md` if you
  haven't set these up yet.

## Task 1 — Start your environment (10 min)

```bash
docker compose up -d
docker compose ps                                   # postgres should be "running"
cd infrastructure/config-server && mvn spring-boot:run &
cd infrastructure/eureka-server  && mvn spring-boot:run &
```

Verify:
- http://localhost:8761 → Eureka dashboard loads (no services registered yet)
- http://localhost:8888/actuator/health → `{"status":"UP"}`

## Task 2 — Build Product Service (25 min)

Open `services/product-service/`. The module already has:

- `pom.xml` — all dependencies configured
- `ProductServiceApplication.java` — the entry point
- `Product.java` — the domain record (`id`, `name`, `description`, `price`, `category`)
- `application.yml` — Eureka + Config Server wiring already in place

Your job is in `ProductService.java` and `ProductController.java`. If
you're working from a fresh skeleton (not the reference branch), you'll
find the method signatures with `// TODO` comments — implement:

1. An in-memory `Map<Long, Product>` as the store
2. `findAll()` → all products
3. `findById(Long id)` → `Optional<Product>`
4. `save(Product product)` → assigns an id if missing, stores, returns it
5. `deleteById(Long id)`

And in the controller, wire up:

- `GET /api/v1/products` → all products
- `GET /api/v1/products/{id}` → 404 if not found
- `POST /api/v1/products` → 201 Created
- `DELETE /api/v1/products/{id}`

## Task 3 — Unit tests (10 min)

In `ProductServiceTest.java`, write (minimum 3, this repo ships 4):

1. `findAll()` returns empty list when no products exist
2. `save()` stores a product and `findById()` retrieves it
3. `findById()` returns empty Optional for a non-existent id
4. (bonus) `deleteById()` removes the product

## Task 4 — Verify service registration (5 min)

```bash
cd services/product-service && mvn spring-boot:run
```

- Open http://localhost:8761 — `PRODUCT-SERVICE` should appear
- `curl http://localhost:8081/api/v1/products` → `[]`
- `curl -X POST http://localhost:8081/api/v1/products -H "Content-Type: application/json" -d '{"name":"Laptop","description":"15-inch","price":999.99,"category":"Electronics"}'`
  → `201 Created`

## Acceptance criteria

1. `docker compose ps` shows postgres healthy
2. `PRODUCT-SERVICE` appears in the Eureka dashboard
3. `GET /api/v1/products` → 200 OK
4. `POST /api/v1/products` → 201 Created
5. `GET /api/v1/products/{id}` → 404 for a non-existent id
6. All unit tests pass: `mvn test`
7. Code pushed with commit message: `session-01: add-product-service-eureka-config`

## Optional homework — JPA + PostgreSQL persistence

Replace the in-memory `Map` with real persistence:

- Add `spring-boot-starter-data-jpa` to `pom.xml`
- Turn `Product` into a JPA `@Entity` (you'll need a class, not a record, for JPA)
- Create a `ProductRepository extends JpaRepository<Product, Long>`
- Update `ProductService` to delegate to the repository
- Add `@DataJpaTest` coverage — minimum 2 test cases
