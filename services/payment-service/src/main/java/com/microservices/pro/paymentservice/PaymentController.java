package com.microservices.pro.paymentservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;

/**
 * PaymentController — Session 4, Lab 3A, Task 1.
 *
 * Simulates an unstable payment gateway:
 *   - payment.failure-rate (Session 4): random chance the call throws instead
 *     of succeeding, so Order Service has real failures to protect against.
 *   - payment.delay-ms (Session 5): optional artificial response delay, used
 *     to demonstrate TimeLimiter cutting off a slow-but-not-down call. Zero
 *     by default — Session 4 never sets this, only Session 5's live coding does.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${payment.failure-rate:0.5}")
    private double failureRate;

    @Value("${payment.delay-ms:0}")
    private long delayMs;

    private final Random random = new Random();

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request)
            throws InterruptedException {

        if (delayMs > 0) {
            Thread.sleep(delayMs);  // Session 5 — simulate a slow (not down) service
        }

        if (random.nextDouble() < failureRate) {
            throw new RuntimeException("Payment gateway timeout");
        }

        return ResponseEntity.ok(new PaymentResponse(
                UUID.randomUUID().toString(),
                "APPROVED",
                request.amount()
        ));
    }
}
