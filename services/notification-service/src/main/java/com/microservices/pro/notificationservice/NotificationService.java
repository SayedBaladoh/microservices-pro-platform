package com.microservices.pro.notificationservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

/**
 * NotificationService — Session 13, Lab 11A.
 *
 * Listens on "payment-events" (the existing S7 topic) for payment results
 * and simulates sending customer notifications. This is the Session 7
 * bonus homework fully implemented.
 *
 * Production pattern — @RetryableTopic:
 *   If processing fails (e.g. email gateway unavailable), Spring Kafka
 *   automatically retries via intermediate retry topics:
 *     payment-events-retry-0  (after 1 000 ms)
 *     payment-events-retry-1  (after 2 000 ms — multiplier=2.0)
 *     payment-events-retry-2  (after 4 000 ms)
 *     payment-events-dlt      (Dead Letter Topic — manual intervention)
 *
 *   This is NOT the same as Resilience4j @Retry (which retries synchronous
 *   method calls). @RetryableTopic retries at the Kafka consumer level —
 *   the message is re-queued in a dedicated retry topic so the main
 *   "payment-events" consumer is not blocked waiting for the retry.
 *   See Session 13 docx 3.5 "fail-fast vs allow-failure" Design Choice.
 *
 * Consumer group: "notification-service" (distinct from all S7/S12 groups).
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void handlePaymentEvent(ConsumerRecord<String, String> record) {
        String payload = record.value();
        String orderId = record.key();

        log.info("[NOTIFICATION] Received payment event for order: {}", orderId);

        if (payload.contains("PaymentCompleted")) {
            sendOrderConfirmation(orderId);
        } else if (payload.contains("PaymentFailed")) {
            sendPaymentFailureAlert(orderId);
        } else {
            log.debug("[NOTIFICATION] Ignoring event type for order: {}", orderId);
        }
    }

    @DltHandler
    public void handleDeadLetter(ConsumerRecord<String, String> record) {
        log.error("[NOTIFICATION] [DLT] Message exhausted all retries for order: {} — payload: {}",
                record.key(), record.value());
        // Production: persist to dead-letter DB table for manual reprocessing.
        // Alert on-call team via PagerDuty/Slack.
    }

    private void sendOrderConfirmation(String orderId) {
        // Production: call email/SMS gateway API.
        log.info("[NOTIFICATION] ✉ Order confirmation sent for order: {}", orderId);
    }

    private void sendPaymentFailureAlert(String orderId) {
        // Production: call email/SMS gateway API.
        log.warn("[NOTIFICATION] ⚠ Payment failure alert sent for order: {}", orderId);
    }
}
