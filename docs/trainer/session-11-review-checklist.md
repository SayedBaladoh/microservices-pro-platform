# Trainer Review Checklist — Session 11

## Definition of Done

- [ ] `OrderServiceInventoryContractTest` — generates `target/pacts/order-service-inventory-service.json`
- [ ] `InventoryServicePactVerificationTest` — reads the pact file, all interactions verified
- [ ] `OrderServicePaymentWireMockTest` — 3 tests: happy path, 409, verify query params
- [ ] Rename bug demo: trainee renamed `available` → `inStock`, saw provider test fail, reverted
- [ ] Commit: `session-11: add-pact-contract-tests-and-wiremock`

## Critical checks

- [ ] **Pact field names in `@Pact` match `StockCheckResponse` record field names exactly.**
      The docx "Story to Tell" is precisely about the `available` → `inStock` rename.
      Ask the trainee: "What happens if inventory-service renames `available`? Would your
      unit tests catch it?" Answer: No. Only the contract test would.

- [ ] **`@PactFolder` path resolves correctly.** `../order-service/target/pacts` is relative
      to the inventory-service module root. If CI runs with a different working directory,
      the path breaks silently (Pact finds no files, no tests run, test "passes" with 0 verifications).
      Check the CI log: it should show "Verifying a pact between order-service and inventory-service".
      If it shows "0 pacts", the path is wrong.

- [ ] **Consumer test runs BEFORE provider test.** The CI workflow enforces this. A trainee
      who runs them in the wrong order sees: "No pact file found". Ask them to explain why.

- [ ] **WireMock stubs inventory-service (not payment-service).** This repo's order-service
      calls inventory-service via OpenFeign (HTTP). Payment happens via Kafka Saga. A trainee
      who tries to WireMock the payment call has misunderstood which calls are HTTP vs event-driven.
      Ask: "What actual HTTP call does order-service make synchronously?"

- [ ] **`@TestPropertySource` disables Eureka, Kafka, Config Server** in the WireMock test.
      Without these, `@SpringBootTest` fails at startup trying to connect to services that
      aren't running during tests.

- [ ] **`resetForTesting()` is package-private, not public.** If a trainee made it `public`,
      ask why access control matters for test-only helpers. Not a hard deduction, but a
      conversation worth having.

## Grading

Feature 70% / Unit Tests 20% / Code Quality 10%.
