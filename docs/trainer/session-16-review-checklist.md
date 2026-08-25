# Trainer Review Checklist — Session 16

## Definition of Done

- [ ] `k8s/product-service/hpa.yaml` — `autoscaling/v2`, 2-10 replicas, CPU 70%
- [ ] `helm/product-service-chart/` — Chart.yaml + values.yaml + 6 templates
- [ ] `helm install product-service ./helm/product-service-chart` succeeds
- [ ] `kubectl auth can-i get secrets --as=...product-service-sa...` → yes
- [ ] `kubectl auth can-i delete pods --as=...product-service-sa...` → no
- [ ] Commit: `session-16: add-hpa-helm-chart-and-rbac`

## Critical checks

- [ ] **HPA uses `autoscaling/v2`, not `v1`.**
  `autoscaling/v1` only supports CPU. `v2` supports CPU + memory + custom
  metrics. The docx specifies `v2` explicitly. A trainee using `v1` will
  see a deprecation warning in K8s 1.26+.

- [ ] **`resource.requests.cpu` present on the container (from S15).**
  HPA cannot compute CPU utilisation without `requests.cpu`. Symptom:
  `kubectl get hpa` shows `TARGETS: <unknown>/70%`. Ask: "What does
  HPA compare the current CPU usage against?"

- [ ] **Helm `{{ .Release.Name }}` used correctly in resource names.**
  Without it, two `helm install` calls in the same namespace create
  conflicting resource names. Ask: "What happens if you run
  `helm install release-a` and `helm install release-b` with the same chart?"

- [ ] **HPA template wrapped in `{{- if .Values.autoscaling.enabled }}`.**
  This lets operators disable autoscaling for dev environments via
  `--set autoscaling.enabled=false` without editing templates.

- [ ] **RBAC uses Role (namespaced), not ClusterRole.**
  product-service only needs access to its own namespace. ClusterRole
  gives access cluster-wide — this violates least privilege. Ask:
  "What's the difference between Role and ClusterRole?"

- [ ] **`serviceAccountName` set in the Helm deployment template.**
  Without it, the pod uses the `default` ServiceAccount — which has no
  RBAC rules and undermines the entire RBAC exercise.

## Phase 2 complete — what to reinforce

Phase 2 (S9-S16) turned the platform from "runnable locally" to
"production-shaped": containerised (S9), tested at multiple layers
(S10-12), CI/CD automated (S13-14), Kubernetes-deployed with health checks,
autoscaling, and access control (S15-16). Phase 3 adds observability,
security, and service mesh.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
