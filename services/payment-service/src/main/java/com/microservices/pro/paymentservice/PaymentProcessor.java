package com.microservices.pro.paymentservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * PaymentProcessor — Session 7.
 *
 * Domain logic used by PaymentSagaHandler to process a payment for a given
 * orderId, reusing the same configurable failure-rate concept as
 * PaymentController (Session 4) but exposed as a plain method call rather
 * than an HTTP endpoint — the Saga handler reacts to a Kafka event, it
 * doesn't make an HTTP request to itself.
 *
 * Deliberately a separate bean from PaymentController: the controller is
 * the synchronous HTTP-facing simulation used directly by API clients /
 * Session 4-5's resilience demos, this is the Saga-facing path used only
 * by PaymentSagaHandler. They share the same configuration key
 * (payment.failure-rate) by design — one unstable payment gateway concept,
 * exercised through two different entry points.
 */
@Service
public class PaymentProcessor {

    @Value("${payment.failure-rate:0.5}")
    private double failureRate;

    private final Random random = new Random();

    public String processPayment(String orderId) {
        if (random.nextDouble() < failureRate) {
            throw new PaymentException("Payment failed for order " + orderId);
        }
        return UUID.randomUUID().toString();
    }
}
