# Troubleshooting

Common issues for Sessions 1–2, taken directly from the Session Pack
"Common Issues & Solutions" tables.

## Session 1 — Eureka / Config Server / Product Service

| Issue | Solution |
|---|---|
| `PRODUCT-SERVICE` never appears in Eureka | Eureka Server must be fully started (check http://localhost:8761 loads) before starting Product Service. Wait ~30s after starting Product Service for registration to show up. |
| Config Server `optional:configserver:...` doesn't seem to load anything | That's expected if you haven't added a `product-service.yml` config block — the `optional:` prefix means the service starts fine either way. Check `infrastructure/config-server/src/main/resources/configs/product-service.yml` exists. |
| `mvn spring-boot:run` fails with a port conflict | Something else is already using 8081 (Product Service), 8761 (Eureka), or 8888 (Config Server). Find and stop it: `lsof -i :8081` (macOS/Linux). |
| Unit tests fail with `NullPointerException` on `productService` | Check `@InjectMocks` is on the field and the test class has `@ExtendWith(MockitoExtension.class)`. |

## Session 2 — API Gateway

| Issue | Solution |
|---|---|
| "Spring Web and Spring WebFlux are conflicting" at startup | Remove `spring-boot-starter-web` from `api-gateway/pom.xml`. Gateway uses WebFlux only — this combination is **not allowed**, by design, in this module. |
| Gateway returns 503 Service Unavailable | `PRODUCT-SERVICE` is not registered in Eureka yet. Start Product Service first and wait ~30s. |
| Route not matching (404 from Gateway) | Check the Path predicate matches exactly: `/api/v1/products/**` (double asterisk required for subpath matching). |
| `lb://` URI causes `NoRouteToHostException` | The service name in the URI (`PRODUCT-SERVICE`) must match the Eureka registration name exactly (case-insensitive by default). |
| `LoggingFilter` not printing logs | Check your log level — add `logging.level.com.microservices.pro=DEBUG` to `application.yml` if needed. |

## Session 3 — JWT Auth Filter & Rate Limiting

| Issue | Solution |
|---|---|
| `IllegalStateException: No KeyResolver bean` | Add `@Primary` to your main `KeyResolver` bean in `RateLimitConfig`. Spring cannot choose between multiple `KeyResolver` beans without it. |
| Rate limiter not triggering (always 200) | Check Redis is running: `docker compose ps`. Also verify `redis-rate-limiter.*` keys are correctly indented under the `RequestRateLimiter` filter in `application.yml`. |
| Every valid-looking token returns 401 | Your `jwt-generator` secret and the Gateway's `jwt.secret` don't match. Set `JWT_SECRET` identically in both terminals — see `docs/labs/session-03-jwt-testing.md`. |
| `JwtException: JWT strings must contain exactly 2 period characters` | Token is malformed — usually a missing `Bearer ` prefix strip. Confirm `token = authHeader.substring(7)`. |
| Public route returning 401 | Check `isPublicRoute()` uses `path.startsWith()`, not `path.equals()` — the path may include trailing segments. |

## Session 4 — Circuit Breaker & Retry

| Issue | Solution |
|---|---|
| `@CircuitBreaker` annotation has no effect | Missing `spring-boot-starter-aop` dependency in `order-service/pom.xml`. AOP is required for Resilience4j annotations to work — see the critical note at the top of that `pom.xml`. |
| Fallback method never called | Fallback signature mismatch. Must have the same params as the original method PLUS `Throwable` (or the specific exception type) as the last param. Method must be in the same class. |
| CB never opens (always `CLOSED`) | `sliding-window-size` too large relative to your test requests. Try `sliding-window-size: 5` and `failure-rate-threshold: 50` for a faster demo. |
| Retry not visible in logs | Add `RetryLogger` (already provided in this repo) — or verify `resilience4j.retry` instance name matches `@Retry`'s `name` exactly (`paymentService`). |
| `404` on `/actuator/circuitbreakers` | Add `management.endpoints.web.exposure.include: health,circuitbreakers` to `application.yml` (already present in this repo's shipped config). |

## Session 5 — Bulkhead & TimeLimiter

| Issue | Solution |
|---|---|
| TimeLimiter not triggering on slow payment | Verify `cancel-running-future: true`. Also ensure the method returns `CompletableFuture<...>` — not a regular synchronous return type. |
| `timeoutFallback()` not being called | Fallback return type must be `CompletableFuture<OrderResponse>` — not just `OrderResponse`. Must match the annotated method exactly. |
| Bulkhead never triggers (always passes through) | `max-wait-duration: 0ms` required. Without it, the default is to wait indefinitely — same as having no Bulkhead at all. |
| `BulkheadFullException` not caught by CB | Bulkhead and CircuitBreaker have separate fallbacks. `BulkheadFullException` is caught by `bulkheadFallback()` — it does not propagate to `paymentFallback()`. |
| Concurrent test not triggering Bulkhead | Your HTTP client may be serializing requests. Use a script with background `&` jobs (as in `docs/labs/session-05-lab-3b.md`) for true concurrency. |

## Still stuck?

Compare your code against the `reference` branch (see
`docs/setup/github-workflow.md`) before contacting your trainer.
