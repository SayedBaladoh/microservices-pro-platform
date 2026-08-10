package com.microservices.pro.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrderServicePaymentWireMockTest — Session 11, Lab 9B, Task 3.
 *
 * 🎯 DESIGN CHOICE: WireMock vs Mockito — scope adaptation for this repo.
 *
 * The Session 11 docx live-coding example stubs payment-service via WireMock.
 * In THIS repository, payment-service is NOT called via HTTP from order-service
 * (it is invoked via Kafka as part of the Choreography Saga — Session 7).
 * The only HTTP call order-service makes via OpenFeign is to inventory-service.
 *
 * WireMock is correct here precisely because: order-service → inventory-service
 * uses OpenFeign (a real HTTP client). Mockito cannot test:
 *   - The correct URL path (/api/v1/inventory/check)
 *   - The correct query params (?productId=...&quantity=...)
 *   - JSON deserialization of the response body into StockCheckResponse
 *
 * WireMock stubs the inventory-service HTTP layer and verifies all of the above.
 *
 * @AutoConfigureWireMock(port = 0): starts WireMock on a random port.
 * @TestPropertySource overrides the Eureka URL that Feign would use to
 * discover inventory-service, pointing it at the WireMock server instead.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        // Override Feign's target URL for INVENTORY-SERVICE to hit WireMock instead
        "spring.cloud.openfeign.client.config.INVENTORY-SERVICE.url=http://localhost:${wiremock.server.port}",
        // Disable Eureka — WireMock does not register with Eureka
        "eureka.client.enabled=false",
        // Disable Kafka — not needed for this HTTP-layer test
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        // Disable Config Server
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        // Disable Redis (Cache)
        "spring.cache.type=none",
})
class OrderServicePaymentWireMockTest {

    @Autowired
    private OrderService orderService;

    // ── Happy Path: Inventory says available → order proceeds ──────────

    @Test
    void createOrder_proceedsPastStockCheck_whenInventoryReportsAvailable() {
        stubFor(get(urlPathEqualTo("/api/v1/inventory/check"))
                .withQueryParam("productId", equalTo("PROD-001"))
                .withQueryParam("quantity",  equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"productId\":\"PROD-001\",\"requestedQuantity\":1," +
                                  "\"available\":true,\"remainingStock\":99}")));

        OrderResponse response = orderService.createOrder(
                new OrderRequest("PROD-001", 1, new BigDecimal("100.00"), "cust-1"));

        // Stock check passed — Saga event published, order returns PENDING
        assertThat(response.status()).isEqualTo("PENDING");
    }

    // ── Failure Path: Inventory returns 409 → order REJECTED ───────────

    @Test
    void createOrder_returnsRejected_whenInventoryReports409() {
        stubFor(get(urlPathEqualTo("/api/v1/inventory/check"))
                .withQueryParam("productId", equalTo("PROD-003"))
                .withQueryParam("quantity",  equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"productId\":\"PROD-003\",\"requestedQuantity\":1," +
                                  "\"available\":false,\"remainingStock\":0}")));

        OrderResponse response = orderService.createOrder(
                new OrderRequest("PROD-003", 1, new BigDecimal("50.00"), "cust-1"));

        assertThat(response.status()).isEqualTo("REJECTED");
    }

    // ── Verify: correct URL + query params were sent by Feign ──────────

    @Test
    void createOrder_sendsCorrectQueryParams_toInventoryService() {
        stubFor(get(urlPathEqualTo("/api/v1/inventory/check"))
                .withQueryParam("productId", equalTo("PROD-002"))
                .withQueryParam("quantity",  equalTo("3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"productId\":\"PROD-002\",\"requestedQuantity\":3," +
                                  "\"available\":true,\"remainingStock\":2}")));

        orderService.createOrder(
                new OrderRequest("PROD-002", 3, new BigDecimal("75.00"), "cust-2"));

        // WireMock verify: assert Feign sent the correct HTTP request
        verify(getRequestedFor(urlPathEqualTo("/api/v1/inventory/check"))
                .withQueryParam("productId", equalTo("PROD-002"))
                .withQueryParam("quantity",  equalTo("3")));
    }
}
