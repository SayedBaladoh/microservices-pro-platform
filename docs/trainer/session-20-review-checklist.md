# Trainer Review Checklist — Session 20

## Definition of Done

- [ ] `JwtAuthFilter.java` and `JwtUtil.java` DELETED (not commented out)
- [ ] `spring-boot-starter-oauth2-resource-server` in api-gateway pom.xml
- [ ] `issuer-uri` in application.yml — `jwt.secret` REMOVED
- [ ] Public route (`GET /api/v1/products`) → 200 without token
- [ ] Protected route (`POST /api/orders`) → 401 without token
- [ ] Admin route (`/api/admin/**`) → 403 with customer token, 200 with admin token
- [ ] `OrderServiceTokenClient` caches the token (not fetching on every call)
- [ ] Commit: `session-20: retire-jwtauthfilter-add-oauth2-resource-server-and-client-credentials`

## Critical checks

- [ ] **`keycloakJwtConverter()` maps `realm_access.roles` to `ROLE_*`.**
  Without this, Spring Security reads only the `scope` claim — Keycloak's
  realm roles are invisible and `hasRole("ADMIN")` always returns 403.
  Ask: "What does your JWT's `realm_access.roles` claim contain?"

- [ ] **`@EnableWebFluxSecurity` (not `@EnableWebSecurity`).**
  api-gateway is WebFlux (reactive). Using the servlet annotation compiles
  but silently fails at runtime — security filter chain is never registered.
  Most common S20 mistake on this project.

- [ ] **`JwtAuthFilter.java` deleted, not commented out.**
  A trainee who kept it "just in case" has two competing filter chains.
  Depending on filter ordering, either the old or new one wins — both is
  never the right answer.

- [ ] **`OrderServiceTokenClient` is `@Service` (singleton), not `@Bean`
  returning a new instance every call.**
  Token caching requires a persistent field — a prototype-scoped bean
  creates a new instance on every injection, losing the cache. Ask:
  "How long does your token cache live?"

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
