package com.microservices.pro.paymentservice.events;

import java.math.BigDecimal;

/**
 * OrderPlacedEvent — Session 7.
 *
 * Published by Order Service to "order-events" when an order is created
 * (status PENDING). Starts the Choreography Saga.
 *
 * Deliberately duplicated across order-service / inventory-service /
 * payment-service rather than shared via a common library — this is the
 * documented pattern from Session 6 (and confirmed again in Session 7's
 * own live-coding script): "In production: shared library or separate
 * events module. In this course: duplicate in each service (discussed in
 * Session 14)."
 */
public record OrderPlacedEvent(
        String orderId,
        String productId,
        int quantity,
        BigDecimal amount,
        String customerId
) {}
