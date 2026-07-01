package com.microservices.pro.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order Service — Session 4, extended in Session 5.
 *
 * Calls Payment Service to process payments, protected by the full
 * Resilience4j stack: Bulkhead, TimeLimiter, CircuitBreaker, Retry.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
