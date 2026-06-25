# Session 2 — Lab 2A: API Gateway Setup & Product Route

**Duration:** 30 min in-session + complete as homework if needed
**Repository path:** `infrastructure/api-gateway/`
**Builds on:** Session 1's `services/product-service/`
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

> **Part 2A of 2.** Part 2B (JWT Auth Filter + Rate Limiting) is Session 3 —
> not in scope for this repository yet.

## Objectives

- Create the API Gateway service using Spring Cloud Gateway + WebFlux
- Configure a route for Product Service using `application.yml` (Path predicate)
- Integrate the Gateway with Eureka for service discovery (`lb://` scheme)
- Implement a `GlobalFilter` that logs method, path, and response status
- Add a custom `X-Platform` response header to all responses
- Write unit tests for the `GlobalFilter` (minimum 2 test methods)

## Task 1 — Create & configure the Gateway (15 min)

Open `infrastructure/api-gateway/`. The module already has:

- `pom.xml` — **no `spring-boot-starter-web`** (Gateway is WebFlux/reactive —
  adding the Servlet stack alongside it breaks startup)
- `ApiGatewayApplication.java` — the entry point
- `application.yml` — route configuration:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/v1/products/**
          filters:
            - StripPrefix=0
            - AddResponseHeader=X-Platform, microservices-pro
```

## Task 2 — Implement the Logging GlobalFilter (10 min)

`LoggingFilter.java` implements `GlobalFilter, Ordered`:

- Logs method + path + remote address before forwarding (pre-filter)
- Logs the response status code after the downstream call (post-filter)
- Returns `Ordered.HIGHEST_PRECEDENCE` from `getOrder()` — runs first

## Task 3 — Write unit tests for the filter (5 min)

In `LoggingFilterTest.java` (minimum 2 test methods, this repo ships 3):

1. `getOrder()` returns `Ordered.HIGHEST_PRECEDENCE`
2. `filter()` calls `chain.filter()` (verify via Mockito mock)
3. (bonus) the filter doesn't mutate the request — same HTTP method reaches the chain

## Acceptance criteria

1. Gateway starts on port 8080 without errors
2. `API-GATEWAY` appears in the Eureka dashboard alongside `PRODUCT-SERVICE`
3. `GET http://localhost:8080/api/v1/products` returns the product list
   (proxied from Product Service)
4. Gateway console shows `[GATEWAY] GET /api/v1/products` log line
5. Response includes `X-Platform: microservices-pro` header
6. All unit tests pass: `mvn test`
7. Code committed: `session-02: add-api-gateway-with-product-route-and-logging-filter`

## Homework (recommended)

- Add a second route: `/actuator/services` → forward to each service's
  `actuator/health` (a simple health-check aggregator)
- Explore `GET http://localhost:8080/actuator/gateway/routes` and understand
  what it exposes about your configured routes
