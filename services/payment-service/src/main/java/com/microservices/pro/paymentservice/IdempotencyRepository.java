package com.microservices.pro.paymentservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * IdempotencyRepository — Session 22.
 */
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, String> {

    List<IdempotencyRecord> findByCreatedAtBefore(Instant cutoff);
    // Used by a cleanup @Scheduled job (homework) to delete expired records.
}
