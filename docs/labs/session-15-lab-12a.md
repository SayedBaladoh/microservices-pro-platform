# Session 15 — Lab 12A: Kubernetes Core (Probes, Resources, Secrets)

**Duration:** 2.5 hours online
**Deliverables:** Updated `k8s/product-service/deployment.yaml` + `secret.yaml`
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## Context

S14 deployed product-service to K8s with a minimal manifest (no probes, no
resource limits, no secrets). This lab adds the production essentials.

## Task 1 — Secrets (20 min)

Create `k8s/product-service/secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: product-service-secret
  namespace: ecommerce
type: Opaque
data:
  db-password: cG9zdGdyZXM=   # base64("postgres") — DEV ONLY
```

Apply it:
```bash
kubectl apply -f k8s/product-service/secret.yaml
```

Update `deployment.yaml` to inject it as an env var:
```yaml
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: product-service-secret
      key: db-password
```

**DEV ONLY note:** committing base64-encoded secrets to Git is acceptable
for this course. Production: use Vault (Session 21) or Sealed Secrets.

## Task 2 — Readiness + Liveness Probes (30 min)

Add to the container spec in `deployment.yaml`:

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 10
  failureThreshold: 3

livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8081
  initialDelaySeconds: 60
  periodSeconds: 30
  failureThreshold: 3
```

**⚖ ENGINEERING DECISION — Liveness vs Readiness:**

| | Readiness | Liveness |
|---|---|---|
| Question | "Am I ready for traffic?" | "Am I still alive?" |
| On failure | Removed from Service endpoints | Container restarted |
| Use for | Startup, dependency checks | Deadlock / hung process |
| `initialDelay` | Shorter (30s) — pod stops traffic sooner | Longer (60s) — avoid restart during startup |

**Why `initialDelaySeconds` matters:**
If liveness fires before the JVM finishes starting (typically 30-45s for
Spring Boot), K8s restarts the pod → which triggers liveness again → crash
loop. Setting `initialDelaySeconds: 60` gives the JVM time to start.

Verify:
```bash
kubectl describe pod <product-service-pod> -n ecommerce | grep -A5 "Readiness\|Liveness"
kubectl get pods -n ecommerce   # should show Running, not CrashLoopBackOff
```

## Task 3 — Resource Requests + Limits (20 min)

```yaml
resources:
  requests:
    cpu: "200m"
    memory: "512Mi"
  limits:
    cpu: "1000m"
    memory: "768Mi"
```

**Why both `requests` AND `limits`?**
- `requests` only: pod can use unlimited resources → noisy neighbour problem
- `limits` only: K8s scheduler has no baseline → poor placement decisions
- Both: scheduler knows what to reserve AND container is bounded

Verify the HPA can now work (added in S16 but dependent on requests):
```bash
kubectl top pods -n ecommerce   # requires metrics-server installed
```

## Acceptance criteria

- [ ] `kubectl describe pod -n ecommerce` — readiness and liveness probes shown
- [ ] Pod reaches `Running` status (not `CrashLoopBackOff`)
- [ ] `kubectl get endpoints product-service -n ecommerce` — pod IP listed (ready)
- [ ] `kubectl exec -n ecommerce <pod> -- env | grep DB_PASSWORD` — value injected
- [ ] Commit: `session-15: add-probes-resources-and-secrets-to-k8s-deployment`
