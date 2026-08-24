# Trainer Review Checklist — Session 14

## Definition of Done

- [ ] `k8s/product-service/` — 4 YAML files present and valid
- [ ] `k8s/argocd/product-service-app.yaml` — correct repoURL and path
- [ ] `kubectl get pods -n ecommerce` — pods running
- [ ] ArgoCD UI shows `Synced`
- [ ] 8 stable + 2 canary pods visible
- [ ] Git push → ArgoCD syncs within 3 minutes
- [ ] Commit: `session-14: add-k8s-manifests-and-argocd-gitops`

## Critical checks

- [ ] **`namespace: ecommerce` in all manifests and ArgoCD destination.**
  A trainee who uses `default` namespace will have pods running but ArgoCD
  won't manage them (different namespace = different Application scope).

- [ ] **Service selector uses `app=product-service` only (no `track` label).**
  If a trainee adds `track: stable` to the Service selector, canary pods receive
  zero traffic — defeating the entire purpose of the canary deployment.
  Ask: "Which pods does your Service route to? How do you know?"

- [ ] **ArgoCD `selfHeal: true` and `prune: true` both set.**
  `selfHeal: false` means manual `kubectl` changes are not reverted — GitOps
  guarantee is broken. `prune: false` means deleted manifests leave orphaned
  resources in the cluster.

- [ ] **🎯 Push vs Pull explained with trade-offs.**
  Ask: "Why does ArgoCD (Pull) work better behind a firewall than kubectl-in-CI (Push)?"
  Expected: CI can't reach the cluster, but the cluster can always reach GitHub.

- [ ] **TODOs in `deployment.yaml` present and NOT implemented.**
  Resource limits, readiness/liveness probes belong in Session 15.
  A trainee who pre-implemented them gets credit for initiative but should be
  told why the curriculum separates them.

- [ ] **OBG-001: no Helm, Ingress, RBAC, HPA in submitted files.**
  Those arrive in Sessions 15-16. A trainee who added Helm chart structure or
  an Ingress controller has jumped ahead — acknowledge the knowledge, redirect
  the implementation.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
