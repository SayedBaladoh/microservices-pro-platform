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

## Session 6 — OpenFeign Inter-Service Communication

| Issue | Solution |
|---|---|
| "No Feign client bean found" | Missing `@EnableFeignClients` on `OrderServiceApplication`. Required to scan and register Feign client interfaces. |
| Feign returns 503 on every call | `INVENTORY-SERVICE` not registered in Eureka. Start Inventory Service and wait ~30s. Check with `GET http://localhost:8761/eureka/apps`. |
| Raw `FeignException` instead of a domain exception | `InventoryErrorDecoder` not being picked up as a `@Component` — verify it's in a scanned package. |
| JWT not forwarded to Inventory | `FeignJwtInterceptor` requires `RequestContextHolder` to have the request. Ensure the calling thread isn't an async/background thread without request propagation. |
| Inventory always returns `available=true` | Check `ConcurrentHashMap` initialization — `PROD-003` should have 0 quantity. Verify `hasStock()` math: `availableQuantity - reservedQuantity`. |

## Session 7 — Saga & Kafka

| Issue | Solution |
|---|---|
| Consumer never receives events | Check `spring.kafka.consumer.group-id` is unique per listener (see the consumer group table in `docs/labs/session-07-lab-5a.md`). Verify `auto-offset-reset: earliest` for new consumers. |
| `ClassCastException` deserializing events | Add `spring.json.trusted.packages: "com.microservices.pro.*"` to consumer config (already present in this repo's shipped `application.yml` files). |
| Compensation fires but stock not released | Check consumer group names — `inventory-compensation` must be a DIFFERENT group from `inventory-service`, or one consumer instance ends up processing both. |
| Order stays `PENDING` indefinitely | Kafka consumer not running — check service startup logs. Verify topic names match exactly (case-sensitive) across all three services. |
| Duplicate compensation (inventory released twice) | `releaseStock(orderId)` must be idempotent — check for an existing reservation before processing; this repo's implementation already handles this as a no-op. |

## Session 8 — Redis Caching

| Issue | Solution |
|---|---|
| "No cache manager found" at startup | Missing `@EnableCaching` on `ProductServiceApplication`. |
| `@CacheEvict` not evicting — stale data persists | Verify the key expression matches `@Cacheable`'s key exactly. `products::1` vs `products:1` are different keys. Use SpEL: `key="#id"`. |
| Cache always MISS — never hits Redis | Check `spring.cache.type: redis` in yml. Without this, Spring uses an in-memory cache by default, not Redis — `redis-cli` will show nothing. |
| Cannot connect to Redis (`ConnectionRefused`) | Redis must be running: `docker compose ps \| grep redis`. Verify host=localhost, port=6379. |
| "Serialization failed" when caching | The cached entity must implement `Serializable` (this repo's `Product` already does) — or configure a Jackson serializer for Redis explicitly. |

## Still stuck?

Compare your code against the `reference` branch (see
`docs/setup/github-workflow.md`) before contacting your trainer.
