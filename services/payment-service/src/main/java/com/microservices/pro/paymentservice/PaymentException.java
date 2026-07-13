package com.microservices.pro.paymentservice;

/**
 * PaymentException — Session 7.
 * Thrown by PaymentProcessor when the simulated payment fails.
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
