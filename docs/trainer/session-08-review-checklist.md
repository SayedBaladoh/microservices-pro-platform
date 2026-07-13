# Trainer Review Checklist — Session 8

Use this when reviewing a trainee's submitted `main` branch for Session 8.

## Definition of Done (from Session 8 docx, Section 8)

- [ ] `@Cacheable` on `findById()` — miss-then-hit behavior verified
- [ ] `@Cacheable` on `findAll()` — list cached in Redis
- [ ] `@CacheEvict` on `save()` and `deleteById()`
- [ ] TTL: `redis-cli TTL products::1` shows < 300 seconds
- [ ] Unit tests passing: `mvn test` green
- [ ] Technical debt list written (from the Architecture Clinic discussion)
- [ ] Commit: `session-08: add-redis-caching-to-product-service`

## Code review points — critical checks first

- [ ] **Both `save()` and `deleteById()` call `evictAllProductsCache()`,
      not just their own individual-key eviction.** This is the single
      most common Session 8 mistake (and the docx names it explicitly:
      S08-Q04) — a trainee who only evicts `products::{id}` leaves
      `products::all` stale after every write.
- [ ] `@EnableCaching` is present on `ProductServiceApplication` — without
      it, every `@Cacheable`/`@CacheEvict` annotation is silently ignored,
      and a trainee will see the repository hit on every single call with
      no error explaining why caching "isn't working."
- [ ] `spring.cache.type: redis` is set explicitly — without it, Spring
      defaults to an in-memory cache (ConcurrentMapCacheManager), which
      "works" in the sense that hits/misses still happen, but defeats the
      entire point of the lab (shared, externalized cache) and won't show
      anything in `redis-cli`.
- [ ] If the trainee's repository/entity layer differs from this repo's
      shipped JPA conversion (e.g. they implemented the Session 1 JPA
      homework differently, or are still using an in-memory store), confirm
      their caching annotations are still meaningfully placed in front of
      a real bottleneck — caching in front of a `Map` lookup has no
      teaching value and suggests the Session 1 homework wasn't actually done.

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10% — see
`docs/grading/grading-rubric.md`.

## Architecture Clinic facilitation notes

See `docs/trainer/session-08-architecture-clinic-questions.md` for the
five discussion questions with Expected/Red-Flag answers. Reminders:

- Do not give the "expected" answers immediately — let groups discuss first.
- Probe shallow answers with "why?" or "what would go wrong if we changed that?"
- Compile the Technical Debt Register live; this list carries forward to
  Session 24's Architecture Clinic #2.

## Common shallow-pass patterns to watch for

- A unit test that asserts `findById()` "returns the right product" by
  mocking the repository to always return the same value regardless of
  the cache annotations — this proves the method delegates to the
  repository, not that caching itself is configured. That's fine and
  expected (see the Lab's note on deferring cache-behavior testing to
  Session 10) — just don't let a trainee claim this tests "the cache."
