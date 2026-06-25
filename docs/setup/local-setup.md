# Local Setup

Complete this before Session 1 if you haven't already — see the full
Trainee Onboarding Guide for the detailed version. This page is the
quick-reference for this repository specifically.

## Required tools

| Tool | Version | Verify |
|---|---|---|
| JDK | 21.x LTS | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker Desktop | 4.x+ | `docker --version && docker compose version` |
| Git | 2.x+ | `git --version` |

## Clone and verify

```bash
git clone https://github.com/SayedBaladoh/microservices-pro-platform.git
cd microservices-pro-platform
ls -la
# You should see: docker-compose.yml, infrastructure/, services/, docs/
```

## Start infrastructure

```bash
docker compose up -d
docker compose ps
```

Expected:
```
postgres   running   0.0.0.0:5432->5432/tcp
```

> Only PostgreSQL is containerized at this point in the course. It isn't
> used by Product Service yet (Product Service uses an in-memory store
> through Session 2) — it's here ready for the optional Session 1 JPA
> homework. Eureka Server, Config Server, API Gateway, and Product Service
> all run directly via Maven, not Docker, through Session 8.

## Start the Spring Boot modules (in order)

```bash
# Terminal 1
cd infrastructure/config-server && mvn spring-boot:run

# Terminal 2 (after Config Server is up)
cd infrastructure/eureka-server && mvn spring-boot:run

# Terminal 3 (after Eureka is up)
cd services/product-service && mvn spring-boot:run

# Terminal 4 (Session 2 onward)
cd infrastructure/api-gateway && mvn spring-boot:run
```

## Verify everything works

| Check | URL / Command | Expected |
|---|---|---|
| Config Server health | http://localhost:8888/actuator/health | `{"status":"UP"}` |
| Eureka dashboard | http://localhost:8761 | Dashboard loads |
| Product Service registered | (refresh Eureka dashboard after starting it) | `PRODUCT-SERVICE` listed |
| Product API direct | http://localhost:8081/api/v1/products | `[]` |
| Product API via Gateway (Session 2+) | http://localhost:8080/api/v1/products | `[]` + `X-Platform` header |

If any of these fail, check `docs/setup/troubleshooting.md` before asking
your trainer.
