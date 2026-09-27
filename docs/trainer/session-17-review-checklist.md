# Trainer Review Checklist — Session 17

## Definition of Done

- [ ] `GET /actuator/prometheus` → 200 OK with metrics output
- [ ] `product_findById_seconds_count` appears in Prometheus
- [ ] Zipkin shows traces with correct service names
- [ ] Log lines contain `traceId` as a JSON field
- [ ] Grafana datasource connected (green checkmark)
- [ ] Commit: `session-17: add-observability-zipkin-prometheus-grafana`

## Critical checks

- [ ] **`TimedAspect @Bean` present in `ObservabilityConfig`.** Without it,
  `@Timed` annotations are silently ignored — the method runs but no metric
  is recorded. Symptom: `/actuator/prometheus` has no `product_findById_*`
  entries. Most common S17 mistake.

- [ ] **`management.endpoints.web.exposure.include` contains `prometheus`.**
  Default Spring Boot actuator exposes only `health` and `info`. Without
  explicitly adding `prometheus`, the endpoint returns 404 and Prometheus
  scrapes return errors.

- [ ] **`management.metrics.tags.application` set to `${spring.application.name}`.**
  Without this, all services' metrics are indistinguishable in Prometheus —
  you cannot filter by service name. Every Grafana query becomes meaningless.

- [ ] **`sampling.probability: 1.0` commented as DEV ONLY.**
  A trainee who submits 1.0 without the comment has missed the engineering
  decision. Ask: "What happens at 1.0 in production with 10k req/sec?"

- [ ] **`logback-spring.xml` overrides the default pattern.**
  Verify by checking `docker compose logs product-service` — output should
  be JSON objects, not plain text. If still plain text, logback-spring.xml
  is not being picked up (wrong filename or wrong location).

- [ ] **`observability/prometheus.yml` scrape targets use Docker service
  names** (e.g. `product-service:8081`), not `localhost`. Inside Docker,
  `localhost` in the Prometheus container refers to Prometheus itself.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
