package com.microservices.pro.paymentservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * PaymentProcessor — Session 7.
 *
 * Implement the TODO below. See docs/labs/session-07-lab-5a.md.
 */
@Service
public class PaymentProcessor {

    @Value("${payment.failure-rate:0.5}")
    private double failureRate;

    private final Random random = new Random();

    // TODO: If random.nextDouble() < failureRate, throw a PaymentException.
    //       Otherwise return a random transaction id (UUID.randomUUID().toString()).
    public String processPayment(String orderId) {
        throw new UnsupportedOperationException("TODO: implement processPayment()");
    }
}
