package com.microservices.pro.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Product Service.
 *
 * First domain microservice in the Enterprise E-Commerce Platform.
 * Registers with Eureka and pulls its config from the Config Server.
 *
 * @EnableCaching (Session 8) activates the Spring Cache abstraction —
 * without it, @Cacheable/@CacheEvict on ProductService are silently ignored.
 */
@SpringBootApplication
@EnableCaching
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
