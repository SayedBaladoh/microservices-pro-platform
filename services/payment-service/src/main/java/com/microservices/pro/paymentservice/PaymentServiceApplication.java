package com.microservices.pro.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service — Session 4.
 *
 * A deliberately unstable callee: returns a configurable failure rate
 * (and, from Session 5, a configurable response delay) so Order Service has
 * something real to protect itself against with Resilience4j.
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
