package com.microservices.pro.orderservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OutboxPublisher — Session 22.
 *
 * Polls the outbox_events table every second and publishes unpublished rows
 * to Kafka, then marks them as published.
 *
 * ⚖ ENGINEERING DECISION: Polling Publisher vs Change Data Capture (CDC):
 *   Polling Publisher (this class): simple, uses existing PostgreSQL,
 *     introduces ~1s latency between commit and Kafka publish.
 *     Correct for most platforms; starts here.
 *   CDC (e.g. Debezium): reads database WAL directly, near-zero latency,
 *     no polling queries. Additional infrastructure to operate.
 *     Use when polling overhead or latency become real problems.
 *
 * REQUIRES: @EnableScheduling on a @Configuration class (e.g. OrderServiceApplication).
 *   Without it, @Scheduled methods are silently ignored — same "silent AOP
 *   requirement" pattern as @Timed (Session 17) and Resilience4j (Session 4).
 *
 * At-least-once delivery note: if the JVM crashes after kafkaTemplate.send()
 * but before outboxRepository.save() commits markPublished(), the row will
 * be re-published on the next poll. Kafka consumers must be idempotent. See
 * the S7 Saga's consumer-side idempotency guard — it is still needed.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate    = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findUnpublished();
        if (pending.isEmpty()) return;

        log.debug("[OUTBOX] Publishing {} pending event(s)", pending.size());

        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send("order-events", event.getAggregateId(), event.getPayload());
                event.markPublished();
                outboxRepository.save(event);
                log.info("[OUTBOX] Published and marked: id={} orderId={}",
                        event.getId(), event.getAggregateId());
            } catch (Exception e) {
                log.error("[OUTBOX] Failed to publish event id={} — will retry on next poll",
                        event.getId(), e);
                // Do NOT mark as published — next poll will retry.
            }
        }
    }
}
