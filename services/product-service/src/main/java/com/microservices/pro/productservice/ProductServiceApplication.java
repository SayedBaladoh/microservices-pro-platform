package com.microservices.pro.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Product Service — Session 1.
 *
 * First domain microservice in the Enterprise E-Commerce Platform.
 * Registers with Eureka and pulls its config from the Config Server.
 *
 * In-memory store for now — JPA + PostgreSQL persistence is Session 1
 * homework / addressed properly in Phase 2.
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
