# Trainer Review Checklist — Session 9

Use this when reviewing a trainee's submitted `main` branch for Session 9.

## Definition of Done (from Session 9 docx, Section 8)

- [ ] All 7 core Dockerfiles present and multi-stage
- [ ] All images under 300 MB (`docker images | grep ':local'`)
- [ ] All images run as non-root (`docker run --entrypoint id ... -u` ≠ 0)
- [ ] `docker compose ps` — all 7 Spring Boot services reach `healthy`
- [ ] Eureka dashboard shows all 7 services registered
- [ ] `GET http://localhost:8080/api/v1/products` returns 200 OK
- [ ] Commit: `session-09: dockerize-platform-multi-stage-non-root-healthcheck`

## Code review points — critical checks first

- [ ] **Multi-stage build present in every Dockerfile.** A trainee who
      submitted a single-stage build will show `~650 MB` images. This is
      the most mechanically checkable item — look for two `FROM` lines in
      each Dockerfile. A single `FROM maven:...` and nothing else = incomplete.

- [ ] **`eclipse-temurin:21-jre-jammy` as the runtime base image.**
      The docx and PPTX (`⚖ ENGINEERING DECISION` badge) explicitly chose
      this image — not `openjdk`, not `adoptopenjdk`, not plain `eclipse-temurin:21`.
      The `-jre` variant matters (no compiler = smaller image, reduced attack
      surface); the `-jammy` tag matters (pinned to Ubuntu 22.04 LTS = no
      surprise base updates). Ask a trainee who used a different image to
      justify the difference.

- [ ] **Non-root user uid 1001, group appuser, consistent across all 7
      services.** A trainee who added a non-root user to some services but
      not others has partial credit only on the Code Quality component. The
      uid must be 1001 (not just "any non-root uid") — consistency across
      the platform matters for volume mounts and inter-container operations
      in later sessions.

- [ ] **`rm -rf /var/lib/apt/lists/*` after apt-get install.** Without
      this, the apt cache is baked into the layer, adding ~15–20 MB
      unnecessarily. Every lab deduction here is 1–2 MB that the trainee
      paid to ship forever; in production this is real cost.

- [ ] **`SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092` in docker-compose.yml
      (not `localhost:9092`).** Inside Docker, `localhost` resolves to the
      container itself, not the Kafka container. This is the most common
      cause of "order-service starts but Saga events never arrive" in this
      session. Verify by checking that the environment variable overrides
      the `localhost:9092` from Phase 1's application.yml.

- [ ] **`depends_on` uses `service_healthy` for Spring Boot dependencies,
      `service_started` for Kafka.** A trainee who used `service_healthy`
      on Kafka will see `docker compose up` fail immediately with a
      confusing "no health check defined" error — not an obvious diagnosis
      for someone who doesn't know the distinction yet.

- [ ] **No Kubernetes/Helm/ArgoCD references anywhere.** Run:
      ```
      grep -ri "kubectl\|helm\|argocd\|kustomize" infrastructure/ services/
      ```
      Any result is an OBG-001 violation. That content belongs in Sessions 15-16.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10% (explaining why
multi-stage + non-root + service_healthy vs service_started) — see
`docs/grading/grading-rubric.md`.

Note: the Code Quality weight is higher than usual (20% vs 10%) because
Dockerfile quality is entirely structural — there is no "logic" to test
with unit tests, so the craft of the build itself carries more weight.

## Common shallow-pass patterns to watch for

- A trainee who copied the multi-stage pattern correctly but left
  `USER root` (or no `USER` directive) — the process still runs as root,
  the CI check will catch it but a code review should too.
- A trainee who built working images but never ran `docker compose up -d`
  to verify the full orchestration (individual `docker build` + individual
  `docker run` are not the same as the composed network). The acceptance
  criteria explicitly require `docker compose ps` showing all `healthy`.
- A trainee who set `start_period: 5s` (too short) — Spring Boot takes
  15-40 seconds to start. They will see containers flip between "starting"
  and "unhealthy" in a loop. The reference uses `start_period: 40s`.
