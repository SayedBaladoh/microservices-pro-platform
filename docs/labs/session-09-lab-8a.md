# Session 9 — Lab 8A: Dockerize the Platform

**Duration:** 5 hours (Anchor Day — offline)
**Scope:** Containerize all 7 core services, complete the full docker-compose
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## Context

Through Session 8, all services ran directly via `mvn spring-boot:run`.
From Session 9 onward, the platform runs entirely inside Docker. This lab
converts every service to a containerized, orchestrated system.

> **OBG-001:** No Kubernetes, Helm, or ArgoCD in any file this session.
> Orchestration arrives in Sessions 15-16. Everything here is Docker only.

## Task 1 — Understand the single-stage baseline (15 min)

Each service already has a working `Dockerfile` in its directory. Read it,
then build one service to see the baseline size:

```bash
cd services/product-service
docker build -t microservices-pro/product-service:local .
docker images | grep product-service
```

Note the image size — it will be around **650 MB** (the full JDK + Maven
are included). Your job in Task 2 is to fix this.

## Task 2 — Convert to multi-stage builds (60 min)

For each of the 7 Dockerfiles, implement `TODO 1`:

```dockerfile
# Stage 1 — builder (only exists during build)
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2 — runtime (this is what ships)
FROM eclipse-temurin:21-jre-jammy AS runtime
# ...
COPY --from=builder /build/target/<service>-*.jar app.jar
```

After each conversion, check the size:

```bash
docker build -t microservices-pro/<service>:local .
docker images | grep '<service>'
# Expected: ~250 MB (was ~650 MB)
```

**Why `eclipse-temurin:21-jre-jammy`?**
See the `⚖ ENGINEERING DECISION` slide from Session 9 — Temurin is the
Eclipse Foundation's certified, actively maintained OpenJDK distribution.
The `-jre` variant includes only the runtime (no compiler), and `-jammy`
pins to Ubuntu 22.04 LTS for a stable, predictable base.

## Task 3 — Add non-root user and health check (30 min)

For each runtime stage, implement `TODO 2` and `TODO 3`:

```dockerfile
# Install curl for the HEALTHCHECK
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Non-root user — uid/gid 1001 consistent across all services
RUN groupadd --system --gid 1001 appuser \
    && useradd --system --uid 1001 --gid appuser --no-create-home appuser

WORKDIR /app
COPY --from=builder /build/target/<service>-*.jar app.jar
USER appuser

EXPOSE <PORT>

HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -f http://localhost:<PORT>/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Verify the user inside the container:

```bash
docker run --rm --entrypoint id microservices-pro/<service>:local -u
# Must NOT be 0 (root)
```

## Task 4 — Complete docker-compose.yml (60 min)

Open `docker-compose.yml`. The infrastructure services (postgres, redis,
kafka) already have working healthchecks. Add the missing pieces to each
Spring Boot service:

**TODO A — healthcheck:**

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:<PORT>/actuator/health"]
  interval: 10s
  timeout: 5s
  start_period: 40s
  retries: 5
```

**TODO B — depends_on:**

```yaml
depends_on:
  config-server:
    condition: service_healthy
  eureka-server:
    condition: service_healthy
  # add postgres / redis / kafka as needed per service
```

**🎯 DESIGN CHOICE — `service_healthy` vs `service_started`:**

| Condition | When to use |
|---|---|
| `service_healthy` | Service exposes a health endpoint — Docker can verify it's actually ready |
| `service_started` | Service has no health endpoint (e.g. Kafka) — Docker only knows the container started |

Using `service_started` for Spring Boot services means the dependent
container starts too early and gets connection-refused errors at boot.
Using `service_healthy` where no healthcheck is defined causes
`docker compose up` to fail with a confusing error. Know which is which.

**Also:** note that `kafka:9092` in application.yml now needs to be
`kafka:9092` (the service name), not `localhost:9092`. Docker DNS resolves
service names within the `platform-net` network. Each service's
`SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092` environment variable overrides
the yml value.

## Full platform startup

Once all 7 Dockerfiles and docker-compose.yml are complete:

```bash
# Build all images
docker compose build

# Start everything
docker compose up -d

# Monitor startup (takes ~2-3 minutes first time)
docker compose ps
# All should reach "healthy" status

# Verify Eureka sees all services
open http://localhost:8761

# Quick smoke test
curl http://localhost:8080/api/v1/products          # through Gateway
curl http://localhost:8080/actuator/health
```

## Acceptance criteria (Definition of Done)

- [ ] `docker compose build` completes without errors
- [ ] `docker compose up -d && docker compose ps` — all 7 Spring Boot services reach `healthy`
- [ ] `docker images | grep ':local'` — all 7 images under **300 MB**
- [ ] `docker run --rm --entrypoint id <image>:local -u` — NOT 0 (non-root) for each image
- [ ] Eureka dashboard shows all 7 services registered
- [ ] `GET http://localhost:8080/api/v1/products` — 200 OK via Gateway
- [ ] All images built from the same `microservices-pro/<name>:local` tag pattern
- [ ] Commit: `session-09: dockerize-platform-multi-stage-non-root-healthcheck`

## Notification Service (bonus)

If you built `notification-service` as the Session 7 homework bonus, add
a Dockerfile and a `docker-compose.yml` entry for it now using the same
multi-stage pattern. It is deliberately excluded from the core lab and the
CI workflow — containerize it only if you built it.

## Common pitfalls

See `docs/setup/troubleshooting.md` — Session 9 section covers the most
common issues: image size > 500 MB (still single-stage), 502 from Gateway
(wrong Kafka/Redis hostname — `localhost` vs service name), and health
check failures from missing `curl` in the runtime stage.
