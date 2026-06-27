# microservices-pro-platform

**Microservices Course — Spring Boot & Spring Cloud, Professional Edition**

This is the **Enterprise E-Commerce Platform** — the one system you build,
session after session, from Session 1 to Session 29. There are no
disconnected practice exercises here: every lab adds a real capability to
this real platform.

**Current scope of this repository: Sessions 1–3.** Services and
infrastructure for later sessions (Order, Payment, Inventory, Notification,
Kafka, Kubernetes, etc.) are added only in the session that teaches them —
see `docs/architecture/platform-overview.md`.

---

## What's in here right now

| Module | Port | Added in | What it does |
|---|---|---|---|
| `infrastructure/eureka-server` | 8761 | Session 1 | Service discovery registry |
| `infrastructure/config-server` | 8888 | Session 1 | Centralized configuration |
| `services/product-service` | 8081 | Session 1 | Product catalogue REST API |
| `infrastructure/api-gateway` | 8080 | Session 2-3 | Single entry point, routing, JWT auth, rate limiting |

`tools/jwt-generator` also exists, but it is **not a platform service** —
it's a CLI you run manually to produce test JWTs for Session 3 labs. See
`docs/labs/session-03-jwt-testing.md`. It will be replaced by a real
Identity Provider in Session 20.

## How you work in this repository

1. **Fork** this repository to your own GitHub account.
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/SayedBaladoh/microservices-pro-platform.git
   cd microservices-pro-platform
   ```
3. **Lab** — open the lab for the current session in `docs/labs/` and
   implement the TODOs you find in the code (search for `// TODO`).
4. **Test** — run the unit tests for the module you're working on:
   ```bash
   cd services/product-service   # or whichever module
   mvn test
   ```
   Your lab is not complete until `mvn test` is green.
5. **Push** your work to your fork on the `main` branch:
   ```bash
   git add .
   git commit -m "session-01: add-product-service-eureka-config"
   git push origin main
   ```
6. **PR** (if your trainer asks for pull-request-based submission) — open a
   pull request from your fork back to the shared classroom repository.

Stuck? Compare your code against the `reference` branch — it contains the
complete, working instructor implementation for everything currently in
scope. See `docs/setup/github-workflow.md` for how to diff against it
without merging it into your own work.

## Running the platform locally

See `docs/setup/local-setup.md` for full instructions. Quick version:

```bash
docker compose up -d                       # starts PostgreSQL
cd infrastructure/config-server && mvn spring-boot:run &
cd infrastructure/eureka-server  && mvn spring-boot:run &
cd services/product-service      && mvn spring-boot:run &
cd infrastructure/api-gateway    && mvn spring-boot:run &
```

Then open:
- Eureka dashboard: http://localhost:8761
- Product Service via Gateway: http://localhost:8080/api/v1/products

## Commit message convention

```
session-NN: short-description-of-what-was-done
```

Examples:
```
session-01: add-product-service-eureka-config
session-02: add-api-gateway-with-product-route-and-logging-filter
```

## Repository structure

```
microservices-pro-platform/
├── docker-compose.yml          # PostgreSQL only — see note below
├── docs/                       # Labs, setup guides, architecture notes
├── infrastructure/
│   ├── eureka-server/          # Session 1
│   ├── config-server/          # Session 1
│   └── api-gateway/            # Session 2
└── services/
    └── product-service/        # Session 1
```

> **Why isn't PostgreSQL used by Product Service yet?** Product Service uses
> an in-memory store through Session 2. PostgreSQL is included in
> `docker-compose.yml` now because the Session 1 homework (optional, JPA +
> PostgreSQL persistence) needs it ready to go — see
> `docs/labs/session-01-lab-01.md`.

## Grading

See `docs/grading/grading-rubric.md`. Each lab is scored: Feature 70% / Unit
Tests 20% / Code Quality 10%.

---

*Dr.Sayed Baladoh · microservices-pro-platform*
