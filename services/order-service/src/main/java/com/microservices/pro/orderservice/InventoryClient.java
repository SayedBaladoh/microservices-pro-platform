package com.microservices.pro.orderservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * InventoryClient — Session 6.
 *
 * Declarative Feign client — Feign resolves "INVENTORY-SERVICE" via Eureka
 * at request time (lb:// load balancing happens automatically, no manual
 * URI needed).
 */
@FeignClient(
        name = "INVENTORY-SERVICE",
        path = "/api/v1/inventory"
)
public interface InventoryClient {

    @GetMapping("/check")
    StockCheckResponse checkStock(
            @RequestParam("productId") String productId,
            @RequestParam("quantity") int quantity
    );
}
