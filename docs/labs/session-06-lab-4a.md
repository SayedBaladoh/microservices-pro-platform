# Session 6 — Lab 4A: Inventory Service + OpenFeign Order Call

**Duration:** 30 min in-session + complete as homework if needed
**Part:** 4A of 2 — Part 4B in Session 7 (Kafka consumer in Inventory)
**New Service:** Inventory Service (port 8084)
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Task 1 — Build Inventory Service (15 min)

`services/inventory-service` already has its module skeleton:

- `pom.xml`, `InventoryServiceApplication.java`, `StockItem.java`,
  `StockCheckResponse.java`, `InsufficientStockException.java`,
  `application.yml` (port 8084)

Implement:

- `InventoryService.checkStock(productId, requestedQty)` — in-memory store
  pre-populated with `PROD-001: 100`, `PROD-002: 5`, `PROD-003: 0`
- `InventoryController`: `GET /api/v1/inventory/check?productId=X&quantity=Y`
  — 200 OK when available, 409 Conflict when not

## Task 2 — Add OpenFeign Client to Order Service (10 min)

In `services/order-service`:

- `pom.xml`: `spring-cloud-starter-openfeign` (already added)
- `OrderServiceApplication.java`: `@EnableFeignClients` (already added)
- `InventoryClient` interface:
  ```java
  @FeignClient(name="INVENTORY-SERVICE", path="/api/v1/inventory")
  public interface InventoryClient {
      @GetMapping("/check")
      StockCheckResponse checkStock(@RequestParam("productId") String productId,
                                     @RequestParam("quantity") int quantity);
  }
  ```
- `OrderService`: call `checkStock()` **before** the payment step. If
  `!stock.available()`, return `OrderResponse("REJECTED", "Insufficient stock")`
- `InventoryErrorDecoder`: handle 409 → `InsufficientStockException`, 404 →
  `ProductNotFoundException`

## Task 3 — JWT Propagation (5 min)

- `FeignJwtInterceptor`: reads `Authorization` from the current request
  (`RequestContextHolder`), forwards it onto every outgoing Feign request.

## Task 4 — Unit Tests (5 min)

`InventoryServiceTest` (minimum 3, this repo ships 3):

1. `checkStock()` returns `available=true` when stock sufficient
2. `checkStock()` returns `available=false` when stock insufficient
3. `hasStock()` logic in `StockItem`

`OrderServiceTest` bonus: integration test with mocked `InventoryClient` —
verify `createOrder()` returns `REJECTED` when the mock reports
`available=false`, and never touches the repository or Kafka.

## Acceptance criteria

1. `INVENTORY-SERVICE` appears in the Eureka dashboard
2. `GET /api/v1/inventory/check?productId=PROD-001&quantity=5` → 200 OK, `available=true`
3. `GET /api/v1/inventory/check?productId=PROD-003&quantity=1` → 409, `available=false`
4. `POST /api/orders` (`PROD-003`) → `OrderResponse status=REJECTED`
5. `POST /api/orders` (`PROD-001`, qty=5) → proceeds past the stock check
6. Inventory Service logs show the incoming `Authorization` header (JWT propagation)
7. All unit tests pass: `mvn test`
8. Commit: `session-06: add-inventory-service-and-feign-client`

## Scope note — what this lab does NOT cover yet

`OrderService.createOrder()` (the method this lab's Feign call lives in)
is replaced/extended in Session 7 to also publish a Kafka event after the
stock check passes — see `docs/labs/session-07-lab-5a.md`. The
synchronous stock pre-check built in this lab is NOT replaced by Kafka —
it stays exactly as built here.

## Homework (recommended)

- Add Inventory Service route to API Gateway: `/api/inventory/** →
  lb://INVENTORY-SERVICE` (documented homework, not part of this lab's
  required scope or this repo's `api-gateway/application.yml`)
- Add PostgreSQL persistence to Inventory Service (replace
  `ConcurrentHashMap` with `JpaRepository`) — not implemented in this repo;
  remains a documented technical debt item, see
  `docs/trainer/session-08-architecture-clinic-questions.md`
