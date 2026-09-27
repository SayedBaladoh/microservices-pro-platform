# Session 21 — Lab 17: Istio — mTLS + Traffic Management

**Duration:** 15 min in-session + homework
**Prerequisites:** Kubernetes cluster running (Sessions 15-16), Istio installed
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## Step 1 — Install Istio (5 min)

```bash
istioctl install --set profile=demo -y
kubectl label namespace ecommerce istio-injection=enabled
kubectl rollout restart deployment -n ecommerce
kubectl get pods -n ecommerce
# All pods should show 2/2 READY (app + istio-proxy sidecar)
```

## Step 2 — Enable STRICT mTLS (5 min)

```bash
kubectl apply -f k8s/istio/peer-authentication.yaml
istioctl authn tls-check product-service.ecommerce.svc.cluster.local
# Should report: mTLS: STRICT
```

Verify: from a pod WITHOUT a sidecar, try to call product-service directly.
Expected: connection refused (STRICT mode rejects non-mTLS callers).

## Step 3 — Weighted Traffic Split (VirtualService + DestinationRule) (5 min)

Requires both stable and canary deployments from Session 14:
```bash
kubectl apply -f k8s/istio/destination-rule-product.yaml
kubectl apply -f k8s/istio/virtual-service-product.yaml
kubectl get virtualservice -n ecommerce
kubectl get destinationrule -n ecommerce
```

Send 100 requests and observe the split:
```bash
for i in {1..20}; do
  curl -s http://localhost:8080/api/v1/products | grep -o '"track":"[^"]*"'
done
# Expect: ~16 stable, ~4 canary (80/20)
```

## 🎯 DESIGN CHOICE — Resilience4j (Session 4) vs Istio outlier detection

| | Resilience4j | Istio outlier detection |
|---|---|---|
| Layer | Application (JVM) | Network (sidecar proxy) |
| Language | Java only | Language-agnostic |
| Fallback | Business-aware (OrderResponse PENDING) | Network-level only (reject connection) |
| Config | annotations + yml | DestinationRule YAML |
| When to use | Domain-specific fallbacks needed | Eject unhealthy pods, polyglot services |

**They are complementary, not competing.** Use both.

## Acceptance criteria

- [ ] All pods show 2/2 READY in `kubectl get pods -n ecommerce`
- [ ] `istioctl authn tls-check` reports STRICT mTLS
- [ ] VirtualService + DestinationRule applied successfully
- [ ] 100-request test shows approximately 80/20 split
- [ ] `outlierDetection` configured on the DestinationRule
- [ ] Commit: `session-21: add-istio-mtls-and-weighted-traffic-split`
