# Session 3 — Testing JwtAuthFilter with the jwt-generator Tool

> **Training Note:** In this session we are VALIDATING JWTs, not ISSUING
> them. Token generation belongs to the Identity Provider (Session 20).
> This JWT Generator tool exists only to simulate an external Identity
> Provider during training, and will be replaced entirely by a real
> Authorization Server (Keycloak/OAuth2) in Session 20.

`tools/jwt-generator` is a standalone CLI — not a platform service. Build
it once, then run it whenever you need a fresh test token.

## Build the tool

```bash
cd tools/jwt-generator
mvn clean package -DskipTests
```

This produces `target/jwt-generator-1.0.0.jar`.

## Before you start: make sure the secret matches

`tools/jwt-generator` and `infrastructure/api-gateway` must sign/verify
with the exact same secret, or every scenario below will fail at Step "201
Created" with a 401 instead — and you won't immediately know why.

Easiest fix: set the same environment variable in the terminal you run
*both* the Gateway and the generator from:

```bash
export JWT_SECRET=microservices-pro-course-dev-secret-key-2026-min-256-bits
```

If you skip this, both modules fall back to the same hardcoded
dev-placeholder value already, so it still works — but setting it
explicitly is the habit worth building now (Vault does this for you in
Session 21).

## Scenario 1 — Admin token (happy path)

```bash
java -jar target/jwt-generator-1.0.0.jar --username=ahmed --roles=ROLE_ADMIN
```

Copy the printed `Authorization: Bearer <token>` line, then:

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <paste-token-here>" \
  -d '{"name":"Laptop","description":"15-inch","price":999.99,"category":"Electronics"}'
```

Expected: `201 Created`. Check the Gateway logs and Product Service logs —
`X-User-Id: ahmed` and `X-User-Role: ROLE_ADMIN` should be visible
downstream.

## Scenario 2 — Customer token, public route (no token needed at all)

```bash
curl http://localhost:8080/api/v1/products
```

Expected: `200 OK` — `GET /api/v1/products` is in `PUBLIC_ROUTES`, so this
works even with zero `Authorization` header. (Generating a customer token
is still useful to confirm a *non-admin* token also works on protected
routes — try the same `curl -X POST` as Scenario 1 but with
`--roles=ROLE_CUSTOMER` instead.)

## Scenario 3 — Expired token

```bash
java -jar target/jwt-generator-1.0.0.jar --username=ahmed --roles=ROLE_ADMIN --expiry=2s
```

Wait at least 2 seconds (read this sentence slowly), then use the printed
token in the same `curl -X POST` as Scenario 1.

Expected: `401 Unauthorized`, body similar to:
```json
{"error": "Invalid or expired token: JWT expired ...", "status": 401}
```

## Scenario 4 — Tampered token

Generate a normal token, then deliberately break its signature by
flipping the last character before using it:

```bash
java -jar target/jwt-generator-1.0.0.jar --username=ahmed --roles=ROLE_ADMIN
# copy the token, then change the very last character to anything else
# e.g. ...AbCd1 → ...AbCd2
```

Use the modified token in the same `curl -X POST` as Scenario 1.

Expected: `401 Unauthorized` — the signature no longer matches the
payload, so `JwtUtil.validateToken()` throws and the filter rejects it.
This is the concrete, hands-on version of "the Gateway validates the
signature locally" — you're watching it actually reject a forged token.

## Scenario 5 (bonus) — No Authorization header at all on a protected route

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Mouse","description":"Wireless","price":19.99,"category":"Electronics"}'
```

Expected: `401 Unauthorized`, body:
```json
{"error": "Missing or invalid Authorization header", "status": 401}
```

## Reminder

This tool will be deleted, not upgraded, when Session 20 introduces a real
Identity Provider. Don't build anything in the platform that depends on
`jwt-generator` existing long-term.
