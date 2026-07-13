# Session 7 — Lab 5A: Order Saga — Happy Path + Payment Compensation

**Duration:** 20 min in-session + complete as homework
**Part:** 5A of 2 — Part 5B in Session 12 (Saga Orchestration + advanced patterns)
**Scope:** Choreography Saga: Order → Inventory → Payment + compensation
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Context — what you're implementing

Order Service, Inventory Service, and Payment Service already exist as
REST services (Sessions 1, 6, 4). This lab replaces the *commitment* step
(reserve stock, charge payment) with a Kafka-based Choreography Saga,
while **keeping** the Session 6 synchronous Inventory pre-check exactly as
it was:

> "The direct OpenFeign inventory check (Lab 4A) can coexist with the
> Saga. Keep it for the stock pre-check. The Saga handles the committed
> reservation."

After this lab:
- `createOrder()` returns `PENDING` immediately (after the sync pre-check passes)
- Kafka events drive the saga: `OrderPlaced → InventoryReserved → PaymentCompleted`
- On `PaymentFailed`: compensation fires automatically: `ReleaseInventory → OrderCancelled`
- Order status can be queried: `GET /api/orders/{orderId}/status`

## Task 1 — Domain Events (5 min)

Already provided in each service's own `events` package (deliberately
duplicated per service — shared libraries are Session 14 scope):

`OrderPlacedEvent`, `InventoryReservedEvent`, `InventoryReservationFailedEvent`,
`PaymentCompletedEvent`, `PaymentFailedEvent`, `InventoryReleasedEvent`,
`OrderConfirmedEvent`, `OrderCancelledEvent`

## Task 2 — Order Service: Publish + Listen (10 min)

`OrderService.createOrder()`:
1. Call `inventoryClient.checkStock()` (Session 6, unchanged) — if
   unavailable, return `REJECTED` immediately, no Order saved, no event published
2. Save the order with status `PENDING`
3. Publish `OrderPlacedEvent` to `"order-events"`
4. Return `OrderResponse(orderId, PENDING, "Order received")`

`OrderSagaEventHandler` (separate class — this is where the docx's own
script places this logic):
- `@KafkaListener(topics="payment-events", groupId="order-service")`:
  `PaymentCompleted` → `CONFIRMED`, `PaymentFailed` → `PAYMENT_FAILED`
- `@KafkaListener(topics="inventory-events", groupId="order-service-cancel")`
  (a **different** group than the listener above):
  `InventoryReleased` → `CANCELLED`

`GET /api/orders/{orderId}/status` returns the current `OrderStatus`.

## Task 3 — Inventory Service: Reserve + Compensate (5 min)

`InventorySagaHandler`:
- `@KafkaListener("order-events", groupId="inventory-service")`: handle
  `OrderPlacedEvent` → `reserveStock()` → publish `InventoryReservedEvent`
  on success, `InventoryReservationFailedEvent` on `InsufficientStockException`
- `@KafkaListener("payment-events", groupId="inventory-compensation")`
  (**must** be a different group than the listener above): handle
  `PaymentFailedEvent` → `releaseStock(orderId)` → publish `InventoryReleasedEvent`

## Task 4 — Payment Service: Complete or Fail (5 min)

`PaymentSagaHandler`:
- `@KafkaListener("inventory-events", groupId="payment-service")`: handle
  `InventoryReservedEvent` **only** (ignore `InventoryReservationFailed` /
  `InventoryReleased` on the same topic) → `processPayment()` → publish
  `PaymentCompletedEvent` or `PaymentFailedEvent`

## Task 5 — Unit Tests (5 min)

`OrderSagaTest` (this repo's name for the docx's `OrderSagaTest.java`):

1. `createOrder()` saves order with `PENDING` status
2. `createOrder()` publishes `OrderPlacedEvent` to `"order-events"`

`OrderSagaEventHandlerTest`:

3. `handlePaymentEvent()` updates order to `PAYMENT_FAILED` on a
   `PaymentFailed` payload

`InventorySagaTest`:

4. `handleOrderPlaced()` publishes `InventoryReservedEvent` when stock is available
5. `handlePaymentFailed()` calls `releaseStock()` and publishes `InventoryReleasedEvent`

## ⚠️ Consumer group checklist (verify before testing live)

Five distinct group IDs must exist across all three services — a typo
that duplicates one silently drops messages instead of throwing an error:

| Service | Listener | Topic | Group ID |
|---|---|---|---|
| order-service | `handlePaymentEvent` | payment-events | `order-service` |
| order-service | `handleInventoryReleased` | inventory-events | `order-service-cancel` |
| inventory-service | `handleOrderPlaced` | order-events | `inventory-service` |
| inventory-service | `handlePaymentFailed` | payment-events | `inventory-compensation` |
| payment-service | `handleInventoryReserved` | inventory-events | `payment-service` |

## Acceptance criteria

1. `POST /api/orders` → returns `{ status: "PENDING", orderId: "..." }` immediately
2. `GET /api/orders/{orderId}/status` → shows `CONFIRMED` after the happy path completes
3. Logs show the full event sequence: `[SAGA] OrderPlaced → InventoryReserved → PaymentCompleted → CONFIRMED`
4. Set `payment.failure-rate=1.0` → order ends with status `CANCELLED`
5. Compensation logs visible: `[SAGA] COMPENSATION: Inventory released`
6. All unit tests pass: `mvn test`
7. Commit: `session-07: add-choreography-saga-order-inventory-payment`

## Homework (recommended)

- Build Notification Service (services/notification-service, port 8085) —
  `@KafkaListener` on `payment-events`, log `"Email sent to customer:
  Order {orderId} confirmed"`. **Not built in this repo** — full
  implementation is Session 13.
- Add the `InventoryReservationFailed` path at the Saga level (what
  happens if Inventory has no stock when the OrderPlaced event itself is
  processed, after the Session 6 pre-check already passed — a race
  condition worth thinking through)
