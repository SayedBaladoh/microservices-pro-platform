# Session 20 — Lab 16: OAuth2 Resource Server + Client Credentials

**Duration:** 10 min in-session + homework
**Services:** api-gateway (primary) + order-service (Client Credentials)
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## ⚖ ENGINEERING DECISION — Clean cutover vs parallel run

| | Clean cutover (today) | Parallel run |
|---|---|---|
| JwtAuthFilter | Deleted | Kept alongside new config |
| Risk | Breaking change if any client still uses old tokens | Safer for live production traffic |
| Clarity | Single clear code path | Two paths to maintain |
| **Our choice** | ✅ Clean cutover — we control all Gateway clients | — |

We choose clean cutover: we control every client (Postman, our own services).
A real production system with external consumers might choose parallel run.

## Task 1 — Delete Session 3 files (5 min)

```bash
rm infrastructure/api-gateway/src/main/java/com/microservices/pro/apigateway/JwtAuthFilter.java
rm infrastructure/api-gateway/src/main/java/com/microservices/pro/apigateway/JwtUtil.java
rm infrastructure/api-gateway/src/main/java/com/microservices/pro/apigateway/JwtConfig.java
```

Remove from `application.yml`: `jwt.secret`, `jwt.public-routes`.

## Task 2 — Add oauth2-resource-server dependency (5 min)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Remove: `io.jsonwebtoken` (jjwt) dependencies.

## Task 3 — Configure application.yml (5 min)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/ecommerce-platform
```

Spring fetches the JWKS from Keycloak automatically at startup.

## Task 4 — SecurityConfig.java (20 min)

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("GET", "/api/v1/products/**").permitAll()
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter()))
            )
            .build();
    }
    // keycloakJwtConverter() maps realm_access.roles → ROLE_ADMIN etc.
}
```

## Task 5 — UserContextEnrichmentFilter.java (10 min)

Replaces JwtAuthFilter's header injection. Reads from Spring Security
principal (already validated) and forwards `X-User-Id` / `X-User-Role`.

## Task 6 — Client Credentials (order-service) (15 min)

Add `order-service` client to Keycloak realm-export.json:
```json
{
  "clientId": "order-service",
  "serviceAccountsEnabled": true,
  "secret": "order-service-secret-dev-only"
}
```

Create `OrderServiceTokenClient.java` — fetches + caches token:
```bash
curl -X POST http://localhost:8180/realms/ecommerce-platform/protocol/openid-connect/token \
  -d 'grant_type=client_credentials' \
  -d 'client_id=order-service' \
  -d 'client_secret=order-service-secret-dev-only'
```

## Acceptance criteria

- [ ] `JwtAuthFilter.java` and `JwtUtil.java` deleted — no compilation errors
- [ ] `GET /api/v1/products` without token → 200 OK (public)
- [ ] `POST /api/orders` without token → 401 Unauthorized
- [ ] `GET /api/admin/test` with customer token → 403 Forbidden
- [ ] `GET /api/admin/test` with admin token → 200 OK
- [ ] `X-User-Id` and `X-User-Role` headers visible in downstream service logs
- [ ] Client Credentials: order-service obtains a token from Keycloak
- [ ] Commit: `session-20: retire-jwtauthfilter-add-oauth2-resource-server-and-client-credentials`
