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

## Still stuck?

Compare your code against the `reference` branch (see
`docs/setup/github-workflow.md`) before contacting your trainer.
