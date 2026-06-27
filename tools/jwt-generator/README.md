# jwt-generator

A standalone CLI tool — **not a platform service.** It is not part of
`infrastructure/` or `services/`, not in `docker-compose.yml`, and not run
by any CI workflow. Build and run it manually whenever you need a test JWT.

> **Training Note:** In this session we are VALIDATING JWTs, not ISSUING
> them. Token generation belongs to the Identity Provider (Session 20).
> This JWT Generator tool exists only to simulate an external Identity
> Provider during training, and will be replaced entirely by a real
> Authorization Server (Keycloak/OAuth2) in Session 20.

## Build

```bash
cd tools/jwt-generator
mvn clean package -DskipTests
```

## Run (default — built jar)

```bash
java -jar target/jwt-generator-1.0.0.jar --username=ahmed --roles=ROLE_ADMIN
```

## Run (alternative — from an IDE / Maven directly)

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--username=ahmed --roles=ROLE_ADMIN"
```

## Arguments

| Flag | Default | Example |
|---|---|---|
| `--username` | `trainee` | `--username=sara` |
| `--roles` | `ROLE_CUSTOMER` | `--roles=ROLE_ADMIN` |
| `--expiry` | `30m` | `--expiry=10s`, `--expiry=1h` |

## Full test scenarios (admin/user/expired/tampered tokens)

See `docs/labs/session-03-jwt-testing.md`.

## Secret synchronization — read this if validation always fails

This tool and `infrastructure/api-gateway` must sign/verify with the exact
same secret. Both read the `JWT_SECRET` environment variable first; if
unset, both fall back to the same hardcoded dev-placeholder string (search
for `DEV ONLY` in `application.yml` and in
`JwtGeneratorApplication.java`). If you changed one without changing the
other, every token will fail signature validation at the Gateway.
