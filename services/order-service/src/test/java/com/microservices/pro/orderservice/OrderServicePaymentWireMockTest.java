package com.microservices.pro.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

/**
 * OrderServicePaymentWireMockTest — Session 11, Lab 9B, Task 3.
 *
 * WireMock stubs inventory-service (the real HTTP Feign call in this repo).
 *
 * 🎯 DESIGN CHOICE: WireMock vs Mockito
 *   WireMock: correct for OpenFeign (real HTTP client) — validates URL,
 *             query params, JSON deserialization.
 *   Mockito:  correct for in-process Java beans (@Service, @Repository).
 *
 * Implement the TODOs below. See docs/labs/session-11-lab-9b.md.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "spring.cloud.openfeign.client.config.INVENTORY-SERVICE.url=http://localhost:${wiremock.server.port}",
        "eureka.client.enabled=false",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "spring.cache.type=none",
})
class OrderServicePaymentWireMockTest {

    @Autowired
    private OrderService orderService;

    @Test
    void createOrder_proceedsPastStockCheck_whenInventoryReportsAvailable() {
        // TODO 1: stubFor(get(urlPathEqualTo("/api/v1/inventory/check"))
        //                 .withQueryParam("productId", equalTo("PROD-001"))
        //                 .withQueryParam("quantity", equalTo("1"))
        //                 .willReturn(aResponse().withStatus(200)
        //                     .withHeader("Content-Type", "application/json")
        //                     .withBody("{ available: true, remainingStock: 99, ... }")))
        // TODO 2: assert orderService.createOrder(...).status() == "PENDING"
        throw new UnsupportedOperationException("TODO: implement happy path WireMock test");
    }

    @Test
    void createOrder_returnsRejected_whenInventoryReports409() {
        // TODO 3: stub 409 response, assert status == "REJECTED"
        throw new UnsupportedOperationException("TODO: implement failure path WireMock test");
    }

    @Test
    void createOrder_sendsCorrectQueryParams_toInventoryService() {
        // TODO 4: stub 200, call createOrder, verify() correct query params were sent
        throw new UnsupportedOperationException("TODO: implement WireMock verify test");
    }
}
