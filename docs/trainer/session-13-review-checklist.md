# Trainer Review Checklist — Session 13

## Definition of Done

- [ ] `product-service-ci.yml` — test job + build-and-push job with `paths:` filter
- [ ] `notification-service-ci.yml` — `@RetryableTopic` + `@DltHandler` guards
- [ ] Notification Service image under 300 MB (multi-stage Dockerfile)
- [ ] `[NOTIFICATION]` logs visible when an order is placed
- [ ] Commit: `session-13: add-cicd-pipelines-and-notification-service`

## Critical checks

- [ ] **`paths:` filter present in both CI workflows.**
  Without it, a documentation commit triggers Docker builds for every service.
  Ask: "What happens if you change README.md — does the product-service image rebuild?"

- [ ] **`@RetryableTopic` + `@DltHandler` both present.**
  `@RetryableTopic` without `@DltHandler` means messages that exhaust all retries
  are silently dropped — no error, no log, no alert. This is the most dangerous
  silent failure mode in Kafka consumers. The CI guard catches it automatically.

- [ ] **`build-and-push` only runs on `push` to `main`, not on PRs.**
  A trainee who omits `if: github.ref == 'refs/heads/main' && github.event_name == 'push'`
  pushes an image on every PR — including failed test branches.

- [ ] **`ghcr.io` registry, not Docker Hub.**
  The docx specifies GitHub Container Registry explicitly. `GITHUB_TOKEN` is
  automatically available — no extra secret setup needed, unlike Docker Hub.

- [ ] **⚖ fail-fast vs allow-failure explained.**
  Ask the trainee: "Why does notification-service-ci.yml not use
  `continue-on-error: true`?" They should reference Session 13's Design Choice
  slide — structural guards (annotation checks) are required gates, not optional.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
