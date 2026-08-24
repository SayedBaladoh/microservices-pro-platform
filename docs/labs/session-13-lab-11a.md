# Session 13 — Lab 11A: CI/CD Pipelines + Notification Service

**Duration:** 2.5 hours online
**Deliverables:** GitHub Actions CI pipeline for product-service + full Notification Service
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## Task 1 — Build the CI pipeline for product-service (45 min)

Open `.github/workflows/product-service-ci.yml`. The pipeline has two jobs:

**Job 1: `test`** — runs `mvn -B test` on every push/PR to `main` that touches `services/product-service/**`.

**Job 2: `build-and-push`** — only runs on `push` to `main` (not on PRs):
```yaml
if: github.ref == 'refs/heads/main' && github.event_name == 'push'
```

Push to `ghcr.io` (GitHub Container Registry):
```yaml
- uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}

- uses: docker/build-push-action@v5
  with:
    context: services/product-service
    push: true
    tags: |
      ghcr.io/OWNER/microservices-pro/product-service:${{ github.sha }}
      ghcr.io/OWNER/microservices-pro/product-service:latest
```

**Why `${{ github.sha }}` + `latest`?**
`latest` for quick reference; `github.sha` for exact reproducibility.
In production, only pin to the SHA — `latest` can mask what's actually running.

**`paths:` filter** — the pipeline only triggers when files under
`services/product-service/**` change. Without this, every commit (including
documentation changes) triggers an unnecessary Docker build.

## Task 2 — Build Notification Service (60 min)

This is the Session 7 "homework bonus" fully implemented.

Open `services/notification-service/`. Create `NotificationService.java`:

```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2.0),
    autoCreateTopics = "true"
)
@KafkaListener(topics = "payment-events", groupId = "notification-service")
public void handlePaymentEvent(ConsumerRecord<String, String> record) {
    String orderId = record.key();
    if (record.value().contains("PaymentCompleted")) {
        log.info("[NOTIFICATION] ✉ Order confirmation sent for: {}", orderId);
    } else if (record.value().contains("PaymentFailed")) {
        log.warn("[NOTIFICATION] ⚠ Payment failure alert sent for: {}", orderId);
    }
}

@DltHandler
public void handleDeadLetter(ConsumerRecord<String, String> record) {
    log.error("[NOTIFICATION] [DLT] Exhausted retries for order: {}", record.key());
}
```

**⚖ ENGINEERING DECISION — `fail-fast` vs `allow-failure`:**

The CI pipeline for notification-service uses `fail-fast` (if tests fail,
the image is never pushed). This is the right choice here — but consider:

| | fail-fast | allow-failure |
|---|---|---|
| When to use | All required quality gates | Optional checks (e.g. performance tests) |
| CI behaviour | Pipeline stops immediately | Pipeline continues, step flagged |
| Risk | Missing a bug in a rare code path | Shipping a build with known issues |

The `@RetryableTopic` and `@DltHandler` guards in `notification-service-ci.yml`
are examples of structural checks — they don't run the code, they verify the
code has the right annotations. This is cheaper than a full integration test
and catches the most common mistake (forgetting DLT handling).

**`@RetryableTopic` retry path:**
```
payment-events
  → payment-events-retry-0  (after 1s)
  → payment-events-retry-1  (after 2s)
  → payment-events-dlt      (dead letter — @DltHandler fires)
```

## Task 3 — Add notification-service to docker-compose.yml (10 min)

Add the `notification-service` block from `docs/labs/session-13-lab-11a.md`
(this file's docker-compose section) to the existing `docker-compose.yml`.

Verify:
```bash
docker compose build notification-service
docker compose up -d notification-service
docker compose logs -f notification-service
# Send a test order: POST /api/orders → watch for [NOTIFICATION] logs
```

## Acceptance criteria

- [ ] `product-service-ci.yml` — tests run on PR, image pushed on merge to main
- [ ] `notification-service-ci.yml` — `@RetryableTopic` + `@DltHandler` guards pass
- [ ] `NOTIFICATION_SERVICE` appears in Eureka dashboard
- [ ] Creating an order → `[NOTIFICATION] ✉ Order confirmation` appears in logs
- [ ] `payment.failure-rate=1.0` → `[NOTIFICATION] ⚠ Payment failure alert` in logs
- [ ] `docker images | grep notification-service` — image under 300 MB
- [ ] Commit: `session-13: add-cicd-pipelines-and-notification-service`
