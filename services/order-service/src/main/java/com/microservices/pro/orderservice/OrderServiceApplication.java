package com.microservices.pro.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Order Service.
 *
 * Calls Payment Service to process payments (Session 4-5 teaching method,
 * not on the main request path after Session 7), checks Inventory
 * synchronously via OpenFeign before confirming an order (Session 6), and
 * drives the Choreography Saga via Kafka for the actual order lifecycle
 * (Session 7).
 *
 * @EnableFeignClients (Session 6) is REQUIRED — without it, Feign will not
 * scan for @FeignClient interfaces (InventoryClient) and no bean is
 * injected at startup.
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
