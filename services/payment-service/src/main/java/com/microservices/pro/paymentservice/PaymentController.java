package com.microservices.pro.paymentservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;

/**
 * PaymentController — Session 4 (original) + Session 22 (idempotency).
 * Implement the TODO. See docs/labs/session-22-lab-18.md.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Value("${payment.failure-rate:0.5}")
    private double failureRate;
    @Value("${payment.delay-ms:0}")
    private int delayMs;

    private final Random random = new Random();
    private final IdempotencyRepository idempotencyRepository;

    public PaymentController(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        // TODO: Add idempotency check BEFORE processing:
        //   1. if key != null && key exists in DB → return cached response
        //   2. if key != null && key not in DB → save as PROCESSING
        //   3. After processing: update record to COMPLETED or FAILED

        // Original Session 4 logic (do not modify):
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        if (random.nextDouble() < failureRate) {
            return ResponseEntity.internalServerError()
                    .body(new PaymentResponse("FAILED", null));
        }
        return ResponseEntity.ok(new PaymentResponse("COMPLETED", UUID.randomUUID().toString()));
    }
}
