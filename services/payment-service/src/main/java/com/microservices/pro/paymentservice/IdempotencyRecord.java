package com.microservices.pro.paymentservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * IdempotencyRecord — Session 22.
 *
 * Stores idempotency keys for payment requests to prevent duplicate charges.
 *
 * Closes the Technical Debt item from Session 4:
 *   "No idempotency on payment retry — duplicate charge risk if Retry
 *    annotation fires the same order ID a second time."
 *
 * Pattern: client supplies an Idempotency-Key header (UUID) with each
 * payment request. PaymentController checks this table first:
 *   - Key found AND request still in-flight: return 202 Accepted
 *   - Key found AND already completed: return the cached response
 *   - Key not found: process the payment, store the result, return it
 *
 * TTL strategy: idempotency records are retained for 24 hours (sufficient
 * for Retry-pattern retries which max out in seconds). A @Scheduled cleanup
 * job (homework) can delete expired records.
 */
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyRecord {

    @Id
    private String idempotencyKey;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String status;         // "PROCESSING" | "COMPLETED" | "FAILED"

    private String responsePayload; // cached JSON response for COMPLETED/FAILED

    @Column(nullable = false)
    private Instant createdAt;

    protected IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String orderId) {
        this.idempotencyKey  = idempotencyKey;
        this.orderId         = orderId;
        this.status          = "PROCESSING";
        this.createdAt       = Instant.now();
    }

    public String getIdempotencyKey()    { return idempotencyKey; }
    public String getOrderId()           { return orderId; }
    public String getStatus()            { return status; }
    public String getResponsePayload()   { return responsePayload; }
    public Instant getCreatedAt()        { return createdAt; }

    public void complete(String responsePayload) {
        this.status          = "COMPLETED";
        this.responsePayload = responsePayload;
    }

    public void fail(String reason) {
        this.status          = "FAILED";
        this.responsePayload = reason;
    }
}
