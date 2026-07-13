package com.microservices.pro.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Inventory Service — Session 6, extended in Session 7 (Saga + Kafka).
 *
 * Provides a synchronous stock-check endpoint (Session 6, called via
 * OpenFeign from Order Service) and, from Session 7, also participates in
 * the Choreography Saga as a Kafka consumer/producer (reserve on
 * OrderPlaced, release on PaymentFailed).
 */
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
