# Trainer Review Checklist — Session 15

## Definition of Done

- [ ] `secret.yaml` present, `db-password` key in base64
- [ ] `deployment.yaml` — readinessProbe + livenessProbe + resources + secretKeyRef
- [ ] Pod reaches `Running` (not `CrashLoopBackOff`)
- [ ] `kubectl get endpoints product-service -n ecommerce` — pod IP listed
- [ ] Commit: `session-15: add-probes-resources-and-secrets-to-k8s-deployment`

## Critical checks

- [ ] **`livenessProbe.initialDelaySeconds` ≥ 60.**
  Less than ~45s → Spring Boot not finished starting → K8s restarts the pod
  → liveness fires again → `CrashLoopBackOff`. This is the most common S15
  mistake. Ask: "Why is `initialDelaySeconds` different for liveness vs readiness?"

- [ ] **`readinessProbe` uses `/actuator/health/readiness`, not `/actuator/health`.**
  `/actuator/health` returns 200 even when dependencies (DB, Redis) are not
  ready — Spring Boot splits readiness and liveness into separate endpoints
  precisely to allow pods to report "alive but not yet ready for traffic."

- [ ] **`resource.requests` present (not just `limits`).**
  Limits without requests: K8s scheduler cannot make placement decisions.
  The HPA (S16) also requires `requests.cpu` to compute utilisation %.

- [ ] **Secret injected via `secretKeyRef`, not hardcoded in env.**
  A trainee who writes `value: "postgres"` directly in the YAML has
  missed the point — the whole exercise is moving secrets OUT of plaintext
  YAML. The secret.yaml approach is still "not production-safe" (committed
  to Git), but it's the right K8s mechanism; Vault (Session 21) fixes Git.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
