# Trainer Review Checklist — Session 19

## Definition of Done

- [ ] Keycloak container healthy: `docker compose ps keycloak`
- [ ] `ecommerce-platform` realm with `gateway` client and `customer1` user
- [ ] Authorization Code Flow completed — code + token exchange evidenced
- [ ] JWT decoded at jwt.io — `iss` and `realm_access.roles` visible
- [ ] Written comparison: S3 JWT vs Keycloak JWT (3-sentence minimum)
- [ ] Commit: `session-19: add-keycloak-realm-client-and-manual-oauth2-flow`

## Critical checks

- [ ] **Client `gateway` is Confidential (not Public).**
  A trainee who set `publicClient: true` has missed the S19 Design Choice.
  Ask: "Why would a Public client be wrong for the Gateway?"

- [ ] **`redirect_uri` in the auth URL exactly matches the one configured
  in Keycloak.** The most common live-session failure — Keycloak rejects
  even trailing-slash differences. Teach: always copy-paste, never retype.

- [ ] **`temporary: false` on the user's password credential.**
  If `temporary: true`, Keycloak forces a password-change redirect on first
  login — fine to allow, but confusing during a demo.

- [ ] **`iss` claim explained in the JWT comparison.**
  The conceptual shift from S3 ("trust because I know the secret") to S19
  ("trust because I trust the issuer") should be explicit in the trainee's
  written comparison — not just a list of claim differences.

- [ ] **No Spring Security code added this session.**
  S19 is setup + manual flow only. `spring-boot-starter-oauth2-resource-server`
  and Gateway wiring belong to S20. A trainee who pre-implemented Resource
  Server config should acknowledge the sequence and explain S20's scope.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
