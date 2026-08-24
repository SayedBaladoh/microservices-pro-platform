package com.microservices.pro.notificationservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * NotificationService — Session 13, Lab 11A.
 *
 * Implement the TODOs below. See docs/labs/session-13-lab-11a.md.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // TODO 1: Add @RetryableTopic annotation:
    //   attempts = "3"
    //   backoff = @Backoff(delay = 1000, multiplier = 2.0)
    //   autoCreateTopics = "true"
    //
    // TODO 2: Add @KafkaListener(topics = "payment-events", groupId = "notification-service")
    //
    // TODO 3: Implement handlePaymentEvent:
    //   - if payload contains "PaymentCompleted" → log order confirmation
    //   - if payload contains "PaymentFailed"    → log payment failure alert
    public void handlePaymentEvent(ConsumerRecord<String, String> record) {
        throw new UnsupportedOperationException("TODO: implement handlePaymentEvent()");
    }

    // TODO 4: Add @DltHandler annotation
    //   Log the orderId and payload for manual reprocessing.
    //   Without this, messages that exhaust all retries are SILENTLY DROPPED.
    public void handleDeadLetter(ConsumerRecord<String, String> record) {
        throw new UnsupportedOperationException("TODO: implement handleDeadLetter()");
    }
}
