# Session 16 — Lab 12B: HPA + Helm Chart + RBAC

**Duration:** 2.5 hours online
**Deliverables:** `hpa.yaml` + complete Helm chart + RBAC manifests
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## Task 1 — HorizontalPodAutoscaler (20 min)

Create `k8s/product-service/hpa.yaml`:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: product-service-hpa
  namespace: ecommerce
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: product-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

**REQUIRES:** `resource.requests.cpu` set on the container (done in S15).
Without it, HPA cannot compute utilisation — it stays at `minReplicas`.

Test:
```bash
kubectl apply -f k8s/product-service/hpa.yaml
kubectl get hpa -n ecommerce
# TARGETS column: <current>%/<target>% — e.g. 12%/70%
```

## Task 2 — Helm Chart (60 min)

**🎯 DESIGN CHOICE — Raw K8s manifests vs Helm:**

| | Raw manifests (S14-15) | Helm chart (S16) |
|---|---|---|
| When to use | Single environment, simple | Multiple environments (dev/staging/prod) |
| Config management | Edit YAML directly | `values.yaml` + `--set` overrides |
| Release history | None | `helm history <release>` |
| Rollback | `kubectl apply` old file | `helm rollback <release> <revision>` |
| Templating | No | Go templates `{{ .Values.key }}` |

```bash
helm/product-service-chart/
├── Chart.yaml
├── values.yaml
└── templates/
    ├── deployment.yaml
    ├── service.yaml
    ├── hpa.yaml
    ├── serviceaccount.yaml
    ├── role.yaml
    └── rolebinding.yaml
```

Key Helm concepts to use:
```yaml
{{ .Release.Name }}           # release name (helm install <name> .)
{{ .Values.replicaCount }}    # from values.yaml
{{ .Chart.Version }}          # from Chart.yaml
{{- if .Values.autoscaling.enabled }}  # conditional block
```

Install and verify:
```bash
helm install product-service ./helm/product-service-chart \
  --namespace ecommerce \
  --create-namespace

helm list -n ecommerce
kubectl get all -n ecommerce -l release=product-service
```

Override for a different environment:
```bash
helm upgrade product-service ./helm/product-service-chart \
  --set replicaCount=3 \
  --set image.tag=abc123sha
```

## Task 3 — RBAC (30 min)

Three files inside the Helm chart templates:

**`serviceaccount.yaml`:** creates `product-service-sa`

**`role.yaml`:** read-only access to ConfigMaps and Secrets:
```yaml
rules:
  - apiGroups: [""]
    resources: ["configmaps", "secrets"]
    verbs: ["get", "list"]
```

**`rolebinding.yaml`:** binds the Role to the ServiceAccount.

Update `deployment.yaml` to use the ServiceAccount:
```yaml
spec:
  serviceAccountName: {{ .Values.serviceAccount.name }}
```

Verify:
```bash
kubectl auth can-i get secrets --as=system:serviceaccount:ecommerce:product-service-sa -n ecommerce
# → yes

kubectl auth can-i delete pods --as=system:serviceaccount:ecommerce:product-service-sa -n ecommerce
# → no
```

## Phase 2 Retrospective (S9 → S16)

| Session | What we added |
|---|---|
| S9  | Docker multi-stage images, docker-compose full stack |
| S10 | @WebMvcTest, @ParameterizedTest, TestContainers |
| S11 | Pact contract tests, WireMock HTTP stubs |
| S12 | Saga Orchestration, SagaState machine |
| S13 | GitHub Actions CI, ghcr.io push, Notification Service |
| S14 | GitOps with ArgoCD, canary deployment |
| S15 | Probes, resource limits, K8s Secrets |
| S16 | HPA, Helm chart, RBAC |

**Platform now:** 8 containerised services, auto-scaling, GitOps-managed,
contract-tested, observable end-to-end.

## Acceptance criteria

- [ ] `kubectl get hpa -n ecommerce` — TARGETS shows actual utilisation
- [ ] `helm list -n ecommerce` — product-service release shown
- [ ] `kubectl get all -n ecommerce -l release=product-service` — all resources present
- [ ] `helm upgrade product-service ./helm/product-service-chart --set replicaCount=3` works
- [ ] `kubectl auth can-i get secrets --as=...product-service-sa...` → yes
- [ ] `kubectl auth can-i delete pods --as=...product-service-sa...` → no
- [ ] Commit: `session-16: add-hpa-helm-chart-and-rbac`
