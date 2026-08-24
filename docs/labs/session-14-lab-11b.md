# Session 14 — Lab 11B: GitOps Deployments with ArgoCD

**Duration:** 2.5 hours online
**Deliverables:** K8s manifests for product-service + ArgoCD Application + canary deployment
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

> **Scope note:** K8s is introduced here as deployment context for GitOps/ArgoCD.
> Full Kubernetes (Deployments, Services, Probes, HPA, RBAC) is Sessions 15-16.
> The manifests in this lab are intentionally minimal — TODOs mark what will
> be completed in S15/S16.

## Task 1 — Write K8s manifests for product-service (30 min)

Open `k8s/product-service/`. Four files to complete:

**`configmap.yaml`** — already provided, no changes needed.

**`service.yaml`** — ClusterIP targeting `app=product-service` (both stable + canary):
```yaml
selector:
  app: product-service   # no track label → routes to ALL pods
type: ClusterIP
```

**`deployment.yaml`** — 8 replicas, label `track: stable`:
```yaml
spec:
  replicas: 8
  selector:
    matchLabels:
      app: product-service
      track: stable
  template:
    metadata:
      labels:
        app: product-service
        track: stable
```

Env vars injected from ConfigMap:
```yaml
env:
  - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
    valueFrom:
      configMapKeyRef:
        name: platform-config
        key: eureka.url
```

**`deployment-canary.yaml`** — 2 replicas, label `track: canary`.
Same structure as stable but with `CANARY_SHA` image tag and `track: canary`.

Apply and verify:
```bash
kubectl apply -f k8s/product-service/ -n ecommerce
kubectl get pods -n ecommerce -l app=product-service
# Expected: 10 pods total (8 stable + 2 canary)
```

## Task 2 — Configure ArgoCD (30 min)

**Install ArgoCD** (if not already done in your cluster):
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl port-forward svc/argocd-server -n argocd 8090:443
```

Open `k8s/argocd/product-service-app.yaml`. Replace `OWNER` with your GitHub username:
```yaml
source:
  repoURL: https://github.com/YOUR_USERNAME/microservices-pro-platform
  targetRevision: main
  path: k8s/product-service
```

Apply the ArgoCD Application:
```bash
kubectl apply -f k8s/argocd/product-service-app.yaml
```

Open the ArgoCD UI at `http://localhost:8090` — verify product-service shows as `Synced`.

## 🎯 DESIGN CHOICE — Push vs Pull Deployment

| | Push (kubectl in CI) | Pull (ArgoCD watches Git) |
|---|---|---|
| How it works | CI pipeline runs `kubectl apply` directly | ArgoCD polls Git, applies changes |
| Cluster access from CI | Required (kubeconfig in secrets) | Not required (ArgoCD runs inside cluster) |
| Drift detection | None — manual `kubectl` changes are silent | ArgoCD detects and alerts/reverts |
| Audit trail | CI logs | Git history (every change = a commit) |
| Works behind firewall | Needs outbound access to cluster | Cluster polls Git (outbound only) |
| **When to use** | Small teams, simple pipelines | Production, regulated environments |

## Task 3 — Canary Deployment (20 min)

The canary pattern is already written in `deployment-canary.yaml`.
To simulate a real canary rollout:

```bash
# 1. Deploy canary (2 of 10 pods = ~20% traffic)
kubectl apply -f k8s/product-service/deployment-canary.yaml -n ecommerce
kubectl get pods -n ecommerce -l app=product-service
# → 8 stable + 2 canary = 10 pods

# 2. Watch logs — confirm canary pods are receiving ~20% of requests
kubectl logs -l app=product-service,track=canary -n ecommerce -f

# 3a. Promote (if healthy) — update stable deployment to canary SHA
#     then scale canary to 0
kubectl scale deployment product-service-canary --replicas=0 -n ecommerce

# 3b. Rollback (if issues) — scale canary to 0 immediately
kubectl scale deployment product-service-canary --replicas=0 -n ecommerce
```

## GitOps demo — trigger ArgoCD sync via git push

```bash
# Change configmap value
echo "  new.key: new-value" >> k8s/product-service/configmap.yaml
git add k8s/product-service/configmap.yaml
git commit -m "session-14: update platform-config"
git push origin main
# → ArgoCD detects the change within ~3 minutes and applies it automatically
```

## Acceptance criteria

- [ ] `kubectl get pods -n ecommerce` — product-service pods running
- [ ] `kubectl get pods -n ecommerce -l track=canary` — 2 canary pods
- [ ] ArgoCD UI shows product-service as `Synced`
- [ ] Git push to `k8s/product-service/` → ArgoCD auto-syncs within 3 min
- [ ] `selfHeal: true` demo: `kubectl delete pod <pod>` → ArgoCD restores it
- [ ] Commit: `session-14: add-k8s-manifests-and-argocd-gitops`

## OBG-001 reminder

Kubernetes RBAC, HPA, Ingress, PVC, Helm, and full probe configuration
are **Session 15-16 scope**. The TODOs in `deployment.yaml` mark exactly
what will be added then. Do not implement them now.
