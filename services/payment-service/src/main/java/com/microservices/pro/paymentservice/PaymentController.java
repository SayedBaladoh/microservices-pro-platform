package com.microservices.pro.paymentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 * Session 22 change: every POST /api/v1/payments MUST include an
 * Idempotency-Key header. The controller checks the header before
 * processing:
 *   - Key already in DB, status COMPLETED: return cached response (200)
 *   - Key already in DB, status PROCESSING: return 202 Accepted
 *   - Key not in DB: process and store
 *
 * Closes Session 4 Technical Debt:
 *   "No idempotency on payment retry — if Resilience4j @Retry fires the
 *    same payment request twice, the customer is charged twice."
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

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

        // ── Idempotency check (Session 22) ────────────────────────────────
        if (idempotencyKey != null) {
            var existing = idempotencyRepository.findById(idempotencyKey);
            if (existing.isPresent()) {
                IdempotencyRecord record = existing.get();
                log.info("[IDEMPOTENCY] Duplicate request detected for key={} status={}",
                        idempotencyKey, record.getStatus());
                if ("COMPLETED".equals(record.getStatus())) {
                    return ResponseEntity.ok(
                            new PaymentResponse("COMPLETED", record.getResponsePayload()));
                }
                return ResponseEntity.accepted()
                        .body(new PaymentResponse("PROCESSING", "Payment already in progress"));
            }
            // New key — register as PROCESSING before doing any work
            idempotencyRepository.save(new IdempotencyRecord(idempotencyKey, request.orderId()));
        }

        // ── Original Session 4 payment logic ──────────────────────────────
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (random.nextDouble() < failureRate) {
            if (idempotencyKey != null) {
                idempotencyRepository.findById(idempotencyKey)
                        .ifPresent(r -> { r.fail("Simulated failure"); idempotencyRepository.save(r); });
            }
            log.warn("[PAYMENT] Failed for orderId={}", request.orderId());
            return ResponseEntity.internalServerError()
                    .body(new PaymentResponse("FAILED", null));
        }

        String transactionId = UUID.randomUUID().toString();
        if (idempotencyKey != null) {
            idempotencyRepository.findById(idempotencyKey)
                    .ifPresent(r -> { r.complete(transactionId); idempotencyRepository.save(r); });
        }
        log.info("[PAYMENT] Completed orderId={} txId={}", request.orderId(), transactionId);
        return ResponseEntity.ok(new PaymentResponse("COMPLETED", transactionId));
    }
}
