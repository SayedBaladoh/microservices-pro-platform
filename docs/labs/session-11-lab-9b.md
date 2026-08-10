# Session 11 — Lab 9B: Contract Testing (Pact) + WireMock

**Duration:** 2.5 hours online
**Services:** `services/order-service` (consumer + WireMock) + `services/inventory-service` (provider)
**Grading:** Feature 70% + Unit Tests 20% + Code Quality 10%

## Context — the bug that Contract Testing catches

Imagine inventory-service renames the response field `available` → `inStock`.
The provider's unit tests still pass (they test the service layer directly).
Order-service's unit tests still pass (they mock the InventoryClient).
**But in production: order-service reads `available`, gets null, treats
every order as REJECTED.** Contract testing catches this at CI time, before
it ever ships.

## Task 1 — Pact Consumer Test (order-service) (30 min)

Open `OrderServiceInventoryContractTest.java`. Implement TODO 1:

```java
@Pact(consumer = "order-service", provider = "inventory-service")
RequestResponsePact checkStockAvailable(PactDslWithProvider builder) {
    return builder
        .given("PROD-001 has 100 units in stock")
        .uponReceiving("a stock check for PROD-001 quantity 5")
            .path("/api/v1/inventory/check")
            .method("GET")
            .query("productId=PROD-001&quantity=5")
        .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(LambdaDsl.newJsonBody(body -> body
                .booleanValue("available", true)
                .integerType("remainingStock", 95)
                .stringValue("productId", "PROD-001")
            ).build())
        .toPact();
}
```

Implement TODO 2-3 (`checkStock_deserializesAvailableField_correctly`):

```java
InventoryClient client = Feign.builder()
    .decoder(new JacksonDecoder())
    .target(InventoryClient.class, mockServer.getUrl());

StockCheckResponse response = client.checkStock("PROD-001", 5);
assertThat(response.available()).isTrue();
```

Run **consumer test first** to generate the pact file:
```bash
mvn test -pl services/order-service -Dtest=OrderServiceInventoryContractTest
ls services/order-service/target/pacts/
# → order-service-inventory-service.json
```

## Task 2 — Pact Provider Verification (inventory-service) (30 min)

Open `InventoryServicePactVerificationTest.java`. Implement the three TODOs:

**TODO 1 — point Pact at the running server:**
```java
context.setTarget(new HttpTestTarget("localhost", port));
```

**TODO 2-3 — @State setup (use the new resetForTesting helper):**
```java
@State("PROD-001 has 100 units in stock")
void setupProd001FullStock() {
    inventoryService.resetForTesting("PROD-001", 100, 0);
}

@State("PROD-003 is out of stock")
void setupProd003OutOfStock() {
    inventoryService.resetForTesting("PROD-003", 0, 0);
}
```

Run the provider verification (pact.json must already exist):
```bash
mvn test -pl services/inventory-service -Dtest=InventoryServicePactVerificationTest
```

**Simulate the rename bug:** In `InventoryController`, temporarily rename
the field `available` → `inStock` in the JSON response. Re-run the provider
test — watch it fail:
```
Expected field 'available' in body but was missing
```
Revert the rename. This is the "aha moment" of contract testing.

## Task 3 — WireMock: Stub InventoryClient (order-service) (45 min)

Open `OrderServicePaymentWireMockTest.java`. Implement the three tests:

**🎯 DESIGN CHOICE: WireMock vs Mockito**

| | WireMock | Mockito |
|---|---|---|
| When to use | Real HTTP clients (Feign, WebClient) | In-process beans (@Service) |
| Tests | URL path, query params, JSON | Method calls, return values |
| Finds | Serialization bugs, wrong URL path | Logic bugs in service layer |

order-service → inventory-service uses OpenFeign (real HTTP) → **WireMock**.

**TODO 1 — Happy path:**
```java
stubFor(get(urlPathEqualTo("/api/v1/inventory/check"))
    .withQueryParam("productId", equalTo("PROD-001"))
    .withQueryParam("quantity",  equalTo("1"))
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("""
            {"productId":"PROD-001","requestedQuantity":1,
             "available":true,"remainingStock":99}
            """)));

OrderResponse r = orderService.createOrder(
    new OrderRequest("PROD-001", 1, new BigDecimal("100.00"), "cust-1"));
assertThat(r.status()).isEqualTo("PENDING");
```

**TODO 2 — Failure path (409):**
Stub 409 → assert `status == "REJECTED"`.

**TODO 3 — Verify query params:**
```java
verify(getRequestedFor(urlPathEqualTo("/api/v1/inventory/check"))
    .withQueryParam("productId", equalTo("PROD-002"))
    .withQueryParam("quantity",  equalTo("3")));
```

## Acceptance criteria

- [ ] `mvn test -pl services/order-service -Dtest=OrderServiceInventoryContractTest`
      generates `target/pacts/order-service-inventory-service.json`
- [ ] `mvn test -pl services/inventory-service -Dtest=InventoryServicePactVerificationTest`
      passes (verifies the contract against the real endpoint)
- [ ] Both `mvn test -pl services/order-service -Dtest=OrderServicePaymentWireMockTest` tests pass
- [ ] Rename bug demonstration: temporarily rename `available` → `inStock` in
      `InventoryController` response, rerun provider test, see it fail, revert
- [ ] Commit: `session-11: add-pact-contract-tests-and-wiremock`

## Scope note — Chaos Engineering demo (Session 11 docx Section 5)

The docx describes a live demo using `payment.failure-rate=1.0` to trigger
circuit breaker and verify resilience. This is a live demo against the
running Docker Compose stack — there is no code deliverable for it. No
files are added to the repo for the chaos demo.
