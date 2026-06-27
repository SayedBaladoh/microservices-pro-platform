# Platform Overview — As of Session 3

This document reflects **only what has been built so far** in this
repository. It is not the full 29-session platform roadmap (that lives in
the Course Kickoff materials, not here) — it will be extended after each
session that adds something new.

## What exists right now

```
                    ┌──────────────────────┐
                    │   CLIENT (Postman)   │
                    └──────────┬───────────┘
                               │ HTTP (+ JWT for protected routes)
                               ▼
                    ┌──────────────────────┐
                    │     API GATEWAY      │   :8080   (Session 2-3)
                    │  LoggingFilter        │
                    │  JwtAuthFilter        │   (Session 3)
                    │  RequestRateLimiter   │   (Session 3)
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

                    ┌──────────────────────┐
                    │        REDIS         │   :6379   (Session 3)
                    │  RequestRateLimiter   │
                    │  token bucket store   │
                    │  (NOT caching yet —    │
                    │   that's Session 8)    │
                    └──────────────────────┘
```

## Services

| Service | Port | Added | Tech | Status |
|---|---|---|---|---|
| Config Server | 8888 | Session 1 | Spring Cloud Config (native backend) | ✅ |
| Eureka Server | 8761 | Session 1 | Spring Cloud Netflix Eureka | ✅ |
| Product Service | 8081 | Session 1 | Spring Boot, in-memory store | ✅ |
| API Gateway | 8080 | Session 2-3 | Spring Cloud Gateway + WebFlux, JWT + Rate Limiting | ✅ |
| Redis | 6379 | Session 3 | Rate limiter token bucket store | ✅ |

## A tool, not a service: `tools/jwt-generator`

This repository also contains `tools/jwt-generator` — a standalone CLI used
to produce test JWTs during Session 3 labs. **It is deliberately not listed
in the table above.** It is not a platform service: it isn't in
`docker-compose.yml`, isn't built or tested by any CI workflow, and the
platform does not depend on it being available at runtime. It exists only
to simulate an Identity Provider during training and will be deleted (not
upgraded) when Session 20 introduces a real one (Keycloak/OAuth2). See
`docs/labs/session-03-jwt-testing.md`.

## What is deliberately NOT here yet

Per the Curriculum Leakage Rule (OBG-001): nothing below exists in this
repository until the session listed actually happens.

| Not yet present | Arrives in |
|---|---|
| Real Identity Provider (Keycloak/OAuth2), login/token-issuance endpoint | Session 20 |
| Order Service, Payment Service | Session 4 |
| Inventory Service, OpenFeign calls | Session 6 |
| Kafka, Saga pattern, Notification Service | Session 7 |
| Redis response caching (@Cacheable) on Product Service | Session 8 |
| Dockerfiles for the Spring Boot services, full containerization | Session 9 |
| Kubernetes, CI/CD, GitOps | Sessions 13–16 |

If you find yourself wanting to "just add" something from this list early —
don't. Open the lab for the session that teaches it instead.
