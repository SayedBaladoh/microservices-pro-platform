# Session 8 — Architecture Clinic #1: Questions for Live Discussion

**This is a discussion document, not a coding exercise.** No code or lab
deliverable accompanies this file — it exists for the trainer to run the
live Architecture Clinic session (Session 8, Part 2, 50 minutes). See
Session 8 docx, Section 3.6-3.9.

## Framing (say this before the workshop)

> "Imagine you are a senior engineer reviewing a colleague's code for the
> first time. You are not here to praise it. You are here to make it
> better. The best engineers are the harshest critics of their own work."

## The Five Questions

### Q1 — Why is Product Service separate from Inventory Service?

**Expected discussion:** Different team ownership, different change rate,
different scaling needs. Products rarely change; inventory changes on
every order.

**Red-flag answer:** *"Because we were told to separate them"* — no
business justification.

### Q2 — Why did we use Saga instead of REST for Order→Payment?

**Expected discussion:** Payment is a write operation — if it fails
mid-flow, we need compensation. REST is request-reply — no built-in
compensation mechanism.

**Red-flag answer:** *"Because Kafka is better than REST"* — wrong
comparison; the question is about consistency guarantees, not transport
speed.

### Q3 — Where did we put the Circuit Breaker and why there?

**Expected discussion:** On Order Service's outgoing call (the
Session 4/5 `createOrderAsync()` teaching method). Order Service cannot
afford to wait for a broken Payment Service — needs to fail fast.

**Red-flag answer:** *"We put it everywhere to be safe"* — no design
intent.

**A note specific to this repo's actual state, worth raising in the
clinic itself:** after Session 7, the real Saga path (`createOrder()`)
does NOT go through the Circuit Breaker at all — Payment is invoked via a
Kafka event, not a direct call. This is a genuinely good clinic discussion
point: ask the group whether the Session 4/5 resilience stack is "wasted
work" now that the Saga exists, or whether it still has documented
teaching value. (Expected answer: it has value — it taught the patterns
themselves, and they remain directly applicable to any future direct
synchronous call this platform adds, such as the Session 6 Inventory
pre-check, which currently has NO Resilience4j protection at all — itself
a good technical debt item to surface here.)

### Q4 — What is the most rushed architectural decision we made?

**Expected discussion:** Open discussion. Good answers: in-memory store
for Inventory, hardcoded public routes in the JWT filter, no idempotency
on payment retry.

**Red-flag answer:** *"Nothing — we designed it perfectly"* — no engineer
ever says this honestly.

### Q5 — If we had to redesign one service boundary, which one?

**Expected discussion:** Open discussion. Examples: should Notification be
part of Order? Should Product and Inventory share a DB for simplicity?

**Red-flag answer:** *"I would not change anything"* — no critical
thinking.

## Technical Debt Register (carry forward to Session 24's Architecture Clinic #2)

Compiled from this repository's actual current state, not just the docx's
generic list:

- In-memory stock map in Inventory Service — no persistence across restarts
- No idempotency on payment retry — duplicate charge risk (Session 4/5 path)
- `PUBLIC_ROUTES` list hardcoded in `JwtAuthFilter` — should be in config
  (documented Session 3 homework, not yet done)
- No dead-letter queue on Kafka consumers — failed events silently dropped
- `payment.failure-rate=0.5` still active by default — leftover test
  configuration, never meant to ship
- No API versioning prefix on Inventory/Order/Payment endpoints (Product
  Service has `/api/v1/`, the others don't — an inconsistency worth raising)
- No database migrations (Flyway/Liquibase) — `ddl-auto: update` is a
  documented DEV ONLY setting on both Product Service and Order Service
- The Session 6 Inventory pre-check (OpenFeign call in `createOrder()`) has
  no Resilience4j protection at all — if Inventory Service is slow, this
  synchronous call blocks with no Circuit Breaker, no TimeLimiter

## Action items for future sessions (do not implement now)

- Idempotency → addressed conceptually in Session 7, implementation work
  belongs in Capstone
- Flyway → recommended homework after Session 9
- DLQ → addressed in Session 12 (Advanced Saga patterns)
- Resilience4j on the Feign Inventory call → worth flagging as a candidate
  for the Capstone's "Advanced Scope," not required Core Scope
