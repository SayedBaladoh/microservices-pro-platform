# Platform Overview — As of Session 8 (Phase 1 Complete)

This document reflects **only what has been built so far** in this
repository. It is not the full 29-session platform roadmap (that lives in
the Course Kickoff materials, not here).

## What exists right now

```
                         ┌──────────────────────┐
                         │   CLIENT (Postman)   │
                         └──────────┬───────────┘
                                    │ HTTP (+ JWT for protected routes)
                                    ▼
                         ┌──────────────────────┐
                         │     API GATEWAY      │   :8080   (S2-3)
                         │  Logging + JwtAuth +   │
                         │  RequestRateLimiter    │
                         └──────────┬───────────┘
                                    │ resolved via Eureka
            ┌───────────────────────┼───────────────────────┐
            ▼                       ▼                       ▼
  ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────────┐
  │ PRODUCT SERVICE  │   │  ORDER SERVICE   │   │  INVENTORY SERVICE   │
  │      :8081        │   │      :8082        │   │       :8084           │
  │  JPA + Redis cache │   │  S4-5: resilience  │   │  S6: stock pre-check  │
  │  (S1 homework+S8)  │   │  stack (unused on  │   │      via Feign        │
  └────────┬───────────┘   │  the Saga path)    │   │  S7: reserve/release  │
           │               │  S6: Feign check   │   │      (Saga steps)     │
           │               │  S7: Saga initiator │   └──────────┬────────────┘
           │               └─────────┬───────────┘              │
           │                         │ Kafka                     │ Kafka
           │                         ▼                            ▼
           │              ┌────────────────────────────────────────────┐
           │              │  KAFKA  (order-events, inventory-events,    │
           │              │          payment-events) — :9092, S7        │
           │              └─────────────────┬──────────────────────────┘
           │                                 │
           │                                 ▼
           │                      ┌──────────────────────┐
           │                      │   PAYMENT SERVICE    │   :8083   (S4, S7)
           │                      │  S4: HTTP callee       │
           │                      │  S7: Saga payment step  │
           │                      └──────────────────────┘
           │
           ▼
  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
  │     REDIS        │    │    POSTGRES      │    │  EUREKA + CONFIG  │
  │  S3: rate limiter  │    │  S1/S8: Product   │    │  S1: registry +    │
  │  S8: response cache│    │  S7: Order entity  │    │      config         │
  └──────────────────┘    └──────────────────┘    └──────────────────┘
```

## A note on Order Service's two order-creation paths

- `createOrderAsync()` — Sessions 4-5's resilience-pattern teaching method
  (Bulkhead → TimeLimiter → CircuitBreaker → Retry around a direct,
  in-process `PaymentService` call). Fully tested, NOT exposed via any
  endpoint after Session 7.
- `createOrder()` — Session 6's Feign stock pre-check + Session 7's actual
  Saga initiator. This is what `POST /api/orders` calls. No Resilience4j
  patterns wrap this method — Kafka's durable log is the Saga's
  resilience mechanism, not Resilience4j around a call that no longer
  exists (Payment is invoked via event, not direct call).

See the header comment on `OrderService.java` for the full rationale, and
`docs/trainer/session-08-architecture-clinic-questions.md` Q3 for the
Architecture Clinic discussion point this raises (is the old resilience
stack "wasted work" now?).

## Services

| Service | Port | Added | Tech | Status |
|---|---|---|---|---|
| Config Server | 8888 | S1 | Spring Cloud Config (native backend) | ✅ |
| Eureka Server | 8761 | S1 | Spring Cloud Netflix Eureka | ✅ |
| Product Service | 8081 | S1, S8 | Spring Boot + JPA/PostgreSQL + Redis cache | ✅ |
| API Gateway | 8080 | S2-3 | Spring Cloud Gateway + WebFlux, JWT + Rate Limiting | ✅ |
| Redis | 6379 | S3, S8 | Rate limiter token bucket store + response cache (two uses) | ✅ |
| Payment Service | 8083 | S4, S7 | Configurable failure rate/delay + Saga payment step | ✅ |
| Order Service | 8082 | S4-7 | Resilience4j stack + Feign + Saga initiator | ✅ |
| Inventory Service | 8084 | S6-7 | Stock pre-check (Feign) + Saga reserve/release | ✅ |
| Kafka + Zookeeper | 9092 | S7 | Choreography Saga event transport | ✅ |
| PostgreSQL | 5432 | S1, S7-8 | Product + Order persistence | ✅ |

## A tool, not a service: `tools/jwt-generator`

Still not a platform service — see `docs/labs/session-03-jwt-testing.md`.
Not in `docker-compose.yml`, not built/tested by any CI workflow.

## What is deliberately NOT here yet

Per the Curriculum Leakage Rule (OBG-001): nothing below exists in this
repository until the session listed actually happens.

| Not yet present | Arrives in |
|---|---|
| Real Identity Provider (Keycloak/OAuth2) | Session 20 |
| Notification Service | Session 7 bonus homework / Session 13 (full) |
| Saga Orchestration (vs today's Choreography) | Session 12 |
| Dead-letter queues on Kafka consumers | Session 12 |
| Dockerfiles for the Spring Boot services, full containerization | Session 9 |
| Database migrations (Flyway/Liquibase) — `ddl-auto: update` is DEV ONLY | Recommended homework after Session 9 |
| Kubernetes, CI/CD, GitOps | Sessions 13–16 |
| Inventory Service persistence (still in-memory) | Documented technical debt, not yet scheduled |

If you find yourself wanting to "just add" something from this list early —
don't. Open the lab for the session that teaches it instead.

## Phase 1 complete

Sessions 1-8 (Foundation & Core Patterns) are now fully represented in this
repository. Phase 2 (Sessions 9-16: Docker, Testing, Kubernetes, CI/CD,
GitOps) begins fresh infrastructure work — see `Microservices_Training_Schedule.docx`.
