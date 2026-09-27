# Session 19 — Lab 15: Keycloak + Authorization Code Flow

**Duration:** 50 min embedded in live coding (no separate lab block)
**Deliverable:** Working Keycloak container + manual token flow evidenced
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

> This session's hands-on work IS the live coding. The steps below are the
> acceptance criteria — complete them during or after the session.

## Step 1 — Start Keycloak (5 min)

The realm config is pre-imported from `keycloak/realm-export.json`:

```bash
docker compose up -d keycloak
docker compose logs -f keycloak
# Wait for: "Keycloak 24.0.4 on JVM ... started in ..."
open http://localhost:8180   # admin / admin
```

Verify the `ecommerce-platform` realm is present with:
- Client `gateway` (Confidential)
- User `customer1` (password: `customer123`)

## Step 2 — Run the Authorization Code Flow (20 min)

**Step 2a — Get the authorization code (browser):**

Open in a browser:
```
http://localhost:8180/realms/ecommerce-platform/protocol/openid-connect/auth
  ?client_id=gateway
  &response_type=code
  &redirect_uri=http://localhost:8080/callback
  &scope=openid
```

Login as `customer1` / `customer123`. The browser redirects to:
```
http://localhost:8080/callback?code=<AUTHORIZATION_CODE>&session_state=...
```

Copy the `code` parameter value.

**Step 2b — Exchange code for tokens (Postman):**

```
POST http://localhost:8180/realms/ecommerce-platform/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&client_id=gateway
&client_secret=gateway-secret-dev-only
&redirect_uri=http://localhost:8080/callback
&code=<AUTHORIZATION_CODE>
```

The response contains `access_token`, `refresh_token`, `id_token`.

## Step 3 — Inspect the JWT (10 min)

Copy the `access_token` value and paste it at https://jwt.io.

Compare with Session 3's hand-signed JWT:

| Claim | Session 3 JWT | Keycloak JWT |
|---|---|---|
| `iss` | absent (no real issuer) | `http://localhost:8180/realms/ecommerce-platform` |
| `sub` | manually set | Keycloak user UUID |
| `realm_access` | absent | `{"roles": ["customer"]}` |
| `aud` | manually set | `["gateway", "account"]` |

**The key insight:** `iss` is now meaningful — the Gateway can verify it
trusts this specific issuer (Keycloak), not just a shared secret.

## ⚖ ENGINEERING DECISION — Public vs Confidential Client

| | Public Client | Confidential Client |
|---|---|---|
| Has client_secret | No | Yes |
| Use for | SPA, mobile apps (code exposed to user) | Server-side apps (Gateway, backend services) |
| Our Gateway client | — | ✅ Confidential (gateway-secret-dev-only) |

**Why Confidential for our Gateway?** The Gateway is a server-side process —
its secret is never sent to the browser. A SPA would use a Public Client +
PKCE instead (no client_secret, PKCE code verifier replaces it).

## Acceptance criteria

- [ ] Keycloak container running: `docker compose ps keycloak` → healthy
- [ ] `ecommerce-platform` realm visible in Admin Console
- [ ] Authorization Code Flow completed: code obtained + tokens exchanged
- [ ] `access_token` decoded at jwt.io — `iss` and `realm_access.roles` visible
- [ ] Written comparison: Session 3 JWT vs Keycloak JWT (3-sentence minimum)
- [ ] Commit: `session-19: add-keycloak-realm-client-and-manual-oauth2-flow`
