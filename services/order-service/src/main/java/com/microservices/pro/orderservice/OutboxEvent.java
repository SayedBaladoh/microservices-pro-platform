package com.microservices.pro.orderservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * OutboxEvent — Session 22.
 *
 * Persisted in the SAME local transaction as the business entity (Order).
 * The OutboxPublisher polls this table and publishes unpublished rows to Kafka.
 *
 * Closing Technical Debt item from Session 8 Architecture Clinic:
 *   "Session 7's Saga has a dual-write gap: orderRepository.save() and
 *   kafkaTemplate.send() are two separate, non-atomic operations."
 *
 * The fix: kafkaTemplate.send() is removed from createOrder() and replaced
 * by a write to this table — inside the same @Transactional boundary as
 * the Order save. If the JVM crashes after the commit, the row remains in
 * the table and the OutboxPublisher will publish it on the next poll.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId;    // orderId

    @Column(nullable = false)
    private String eventType;      // "OrderPlaced"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;        // JSON-serialized event

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean published;

    protected OutboxEvent() {}

    public OutboxEvent(String aggregateId, String eventType, String payload) {
        this.aggregateId = aggregateId;
        this.eventType   = eventType;
        this.payload     = payload;
        this.createdAt   = Instant.now();
        this.published   = false;
    }

    public Long getId()             { return id; }
    public String getAggregateId()  { return aggregateId; }
    public String getEventType()    { return eventType; }
    public String getPayload()      { return payload; }
    public Instant getCreatedAt()   { return createdAt; }
    public boolean isPublished()    { return published; }
    public void markPublished()     { this.published = true; }
}
