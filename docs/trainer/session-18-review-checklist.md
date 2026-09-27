# Trainer Review Checklist — Session 18

## Definition of Done

- [ ] `ProductCommandService` — writes + invariants + publishes event
- [ ] `ProductQueryService` — `@Transactional(readOnly=true)` + projection
- [ ] `ProductCacheEvictionListener` — evicts BOTH cache keys
- [ ] 2 unit tests passing: `mvn test` green
- [ ] Commit: `session-18: add-cqrs-command-query-split-product-service`

## Critical checks

- [ ] **`ProductCommandService` does NOT call `@CacheEvict` directly.**
  Cache eviction is the Listener's job. If a trainee added `@CacheEvict`
  to CommandService, they have not decoupled the Command side from cache
  knowledge — the extension seam (Quiz Q4) is broken.

- [ ] **`ProductQueryService` has `@Transactional(readOnly = true)` on
  the SERVICE method, not only the repository.** Spring applies the proxy
  at the layer where @Transactional is declared. Repository-only annotation
  does not prevent dirty-checking at the service level.

- [ ] **`ProductCacheEvictionListener` evicts BOTH keys** — the individual
  product key AND `'all-summary'`. Missing either leaves stale data.
  Same lesson as Session 8 Q4, now event-driven.

- [ ] **`@Mock ApplicationEventPublisher` present in the test class.**
  Without it, `publishEvent()` throws NullPointerException. Most common
  S18 unit test failure — often the trainee mocks the repository but forgets
  the publisher.

- [ ] **Invariant validation in `ProductCommandService` only.**
  A trainee who added `price > 0` validation to `ProductQueryService` has
  blurred the Command/Query boundary. Query reads valid state — nothing to
  validate. Ask: "What would happen if you validated on the read side?"

## Grading

Feature 70% / Code Quality 20% / Design Choice 10%.
