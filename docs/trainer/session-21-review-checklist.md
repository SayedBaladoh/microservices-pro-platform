# Trainer Review Checklist — Session 21

## Definition of Done

- [ ] All pods show 2/2 READY (sidecar injected)
- [ ] `istioctl authn tls-check` → STRICT mTLS
- [ ] VirtualService + DestinationRule applied
- [ ] ~80/20 traffic split verified (100-request test)
- [ ] `outlierDetection` configured on DestinationRule
- [ ] Commit: `session-21: add-istio-mtls-and-weighted-traffic-split`

## Critical checks

- [ ] **Pods restarted after `istio-injection=enabled` label.**
  The label only applies to NEW pod creation — existing pods remain
  without sidecars (1/1, not 2/2). `kubectl rollout restart deployment`
  is mandatory after labeling.

- [ ] **DestinationRule subsets match actual pod labels.**
  `track: stable` and `track: canary` must match the labels set in
  `deployment.yaml` and `deployment-canary.yaml` (Session 14/15).
  A label mismatch silently routes 100% traffic to the matching subset.

- [ ] **`PeerAuthentication` namespace matches `ecommerce`.**
  Applying to `istio-system` namespace makes it cluster-wide (different
  behaviour). Applying to `default` has no effect on the ecommerce namespace.

- [ ] **🎯 Design Choice verbalized.**
  Ask: "When would you still use Resilience4j's Circuit Breaker alongside
  Istio's outlier detection?" Expected: when a domain-aware fallback response
  (e.g. OrderResponse status=PENDING) is needed — the mesh rejects at the
  network layer and cannot construct a meaningful application response.

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
