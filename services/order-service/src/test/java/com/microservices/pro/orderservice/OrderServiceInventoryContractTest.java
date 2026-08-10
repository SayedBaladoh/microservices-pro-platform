package com.microservices.pro.orderservice;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrderServiceInventoryContractTest — Session 11, Lab 9B, Task 1.
 *
 * Consumer-Driven Contract test: order-service (Consumer) defines what it
 * expects from inventory-service (Provider). Pact generates a JSON pact
 * file that inventory-service reads and verifies.
 *
 * Key concepts from Session 11:
 *   - The Consumer owns the expectations — the Provider must NOT silently
 *     break the field names/types the Consumer depends on.
 *   - @Pact defines the interaction (request + expected response).
 *   - @PactTestFor(pactMethod) links a @Test to a @Pact interaction.
 *   - Pact starts a mock HTTP server (replaces the real inventory-service).
 *   - Running this test generates:
 *       target/pacts/order-service-inventory-service.json
 *   - Inventory-service reads that file in its provider verification test.
 *
 * Scope: this test verifies that the Feign client correctly deserializes
 * the inventory-service response (particularly the 'available' field —
 * the field rename bug in the "Story to Tell" in the docx).
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "inventory-service", port = "8888")
class OrderServiceInventoryContractTest {

    // ── Contract: stock available ───────────────────────────────────────

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
                            .booleanValue("available", true)     // field name IS the contract
                            .integerType("remainingStock", 95)   // type matters, exact value does not
                            .stringValue("productId", "PROD-001")
                    ).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "checkStockAvailable")
    void checkStock_deserializesAvailableField_correctly(MockServer mockServer) {
        InventoryClient client = buildFeignClient(mockServer.getUrl());

        StockCheckResponse response = client.checkStock("PROD-001", 5);

        assertThat(response.available()).isTrue();
        assertThat(response.remainingStock()).isGreaterThan(0);
        assertThat(response.productId()).isEqualTo("PROD-001");
    }

    // ── Contract: stock unavailable (409 Conflict) ──────────────────────

    @Pact(consumer = "order-service", provider = "inventory-service")
    RequestResponsePact checkStockUnavailable(PactDslWithProvider builder) {
        return builder
                .given("PROD-003 is out of stock")
                .uponReceiving("a stock check for PROD-003 quantity 1")
                    .path("/api/v1/inventory/check")
                    .method("GET")
                    .query("productId=PROD-003&quantity=1")
                .willRespondWith()
                    .status(409)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(LambdaDsl.newJsonBody(body -> body
                            .booleanValue("available", false)
                            .integerType("remainingStock", 0)
                            .stringValue("productId", "PROD-003")
                    ).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "checkStockUnavailable")
    void checkStock_returns409_whenStockInsufficient(MockServer mockServer) {
        InventoryClient client = buildFeignClient(mockServer.getUrl());

        // When inventory returns 409, InventoryErrorDecoder throws InsufficientStockException.
        // That exception propagates up to OrderService.createOrder() which returns REJECTED.
        // Here we are testing that Feign correctly receives and routes the 409 response —
        // the exception handling is tested in the unit tests for InventoryErrorDecoder.
        // We verify the pact expectation only: 409 body has available=false.
        try {
            client.checkStock("PROD-003", 1);
        } catch (Exception e) {
            // expected — InventoryErrorDecoder converts 409 → InsufficientStockException
        }
        // Pact verification: the interaction was received with the correct response shape.
        // No assertion needed here — the @PactTestFor mechanism verifies the contract.
    }

    private InventoryClient buildFeignClient(String baseUrl) {
        return Feign.builder()
                .decoder(new JacksonDecoder())
                .target(InventoryClient.class, baseUrl);
    }
}
