package com.microservices.pro.orderservice;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * OutboxPublisher — Session 22, Lab 18.
 * Implement the TODO. See docs/labs/session-22-lab-18.md.
 *
 * REQUIRES: @EnableScheduling on OrderServiceApplication — without it,
 * @Scheduled is silently ignored.
 */
@Component
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate    = kafkaTemplate;
    }

    // TODO: @Scheduled(fixedDelay = 1000) + @Transactional
    //   1. outboxRepository.findUnpublished()
    //   2. for each event: kafkaTemplate.send("order-events", aggregateId, payload)
    //   3. event.markPublished() + outboxRepository.save(event)
    //   Catch exceptions per event — a single failure must NOT stop others.
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        throw new UnsupportedOperationException("TODO: implement publishPendingEvents()");
    }
}
