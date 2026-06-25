package com.microservices.pro.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — Session 2.
 *
 * Single entry point for all external traffic. Routes /api/v1/products/**
 * to Product Service via Eureka (lb://PRODUCT-SERVICE) — see application.yml.
 *
 * Built on Spring Cloud Gateway + WebFlux. Do NOT add spring-boot-starter-web
 * to this module (see pom.xml description).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
