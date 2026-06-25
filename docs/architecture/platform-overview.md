# Platform Overview — As of Session 2

This document reflects **only what has been built so far** in this
repository. It is not the full 29-session platform roadmap (that lives in
the Course Kickoff materials, not here) — it will be extended after each
session that adds something new.

## What exists right now

```
                    ┌──────────────────────┐
                    │   CLIENT (Postman)   │
                    └──────────┬───────────┘
                               │ HTTP
                               ▼
                    ┌──────────────────────┐
                    │     API GATEWAY      │   :8080   (Session 2)
                    │  Route: /api/v1/      │
                    │  products/** →        │
                    │  lb://PRODUCT-SERVICE │
                    └──────────┬───────────┘
                               │ resolved via Eureka
                               ▼
                    ┌──────────────────────┐
                    │   PRODUCT SERVICE    │   :8081   (Session 1)
                    │  In-memory store     │
                    └──────────┬───────────┘
                               │ registers with
                               ▼
                    ┌──────────────────────┐
                    │    EUREKA SERVER     │   :8761   (Session 1)
                    └──────────────────────┘

                    ┌──────────────────────┐
                    │    CONFIG SERVER     │   :8888   (Session 1)
                    │  serves config to all │
                    │  services above        │
                    └──────────────────────┘
```

## Services

| Service | Port | Added | Tech | Status |
|---|---|---|---|---|
| Config Server | 8888 | Session 1 | Spring Cloud Config (native backend) | ✅ |
| Eureka Server | 8761 | Session 1 | Spring Cloud Netflix Eureka | ✅ |
| Product Service | 8081 | Session 1 | Spring Boot, in-memory store | ✅ |
| API Gateway | 8080 | Session 2 | Spring Cloud Gateway + WebFlux | ✅ |

## What is deliberately NOT here yet

Per the Curriculum Leakage Rule (OBG-001): nothing below exists in this
repository until the session listed actually happens.

| Not yet present | Arrives in |
|---|---|
| JWT auth, rate limiting on the Gateway | Session 3 |
| Order Service, Payment Service | Session 4 |
| Inventory Service, OpenFeign calls | Session 6 |
| Kafka, Saga pattern, Notification Service | Session 7 |
| Redis, caching | Session 8 |
| Dockerfiles for the Spring Boot services, full containerization | Session 9 |
| Kubernetes, CI/CD, GitOps | Sessions 13–16 |

If you find yourself wanting to "just add" something from this list early —
don't. Open the lab for the session that teaches it instead.
