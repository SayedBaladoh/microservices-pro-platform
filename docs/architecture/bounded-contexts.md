# Bounded Contexts — As of Session 2

Per Domain-Driven Design (Session 1, Part 2): a Bounded Context is a logical
boundary within which a domain model is defined and consistent. Only one
domain context has real code in this repository so far.

## Product Catalogue context (Session 1)

- **Owns:** `Product` (id, name, description, price, category)
- **Service:** `services/product-service`
- **Why it's its own service:** product catalogue data has a different
  ownership, change rate, and scaling profile than other domains that will
  be added later (e.g. inventory stock levels, which change on every order —
  see Session 6 when Inventory Service is introduced).

## API Gateway — not a domain context

The API Gateway (Session 2) is intentionally **not** a bounded context. It
holds no business logic and owns no domain model — it is a cross-cutting
technical concern (routing, and from Session 3 onward, security and rate
limiting). If you find yourself wanting to put business rules inside a
Gateway filter, that's a sign that logic belongs in a service instead.

## What's not modeled yet

Order, Payment, Inventory, and Notification are real bounded contexts in the
target platform, but none of them have code in this repository yet — they
arrive with their respective sessions (4, 4, 6, and 7). Don't pre-model
their domain objects here; cross-context modeling decisions (e.g. "is
Customer the same model in Order and Notification?") are deliberately
taught in Session 1's DDD discussion, not encoded prematurely in this repo.
