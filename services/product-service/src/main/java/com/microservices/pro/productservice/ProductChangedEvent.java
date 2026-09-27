package com.microservices.pro.productservice;

/**
 * ProductChangedEvent — Session 18.
 *
 * Internal Spring ApplicationEvent published by ProductCommandService
 * after every successful write (create, update, delete). Consumed by
 * ProductCacheEvictionListener to evict stale cache entries.
 *
 * KEY DISTINCTION from Kafka events (Sessions 7, 12, 13):
 *   This event travels INSIDE the JVM — no Kafka, no serialization,
 *   no broker. It is purely a Spring ApplicationEvent, synchronous by
 *   default (listener runs on the same thread as the publisher).
 *   Use @Async + @EnableAsync to decouple if the listener is slow.
 *
 * changeType values: "CREATED", "UPDATED", "DELETED"
 */
public record ProductChangedEvent(Long productId, String changeType) {}
