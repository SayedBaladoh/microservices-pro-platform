package com.microservices.pro.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service — Session 13.
 *
 * Kafka consumer that listens for PaymentCompleted/PaymentFailed events
 * and simulates sending customer notifications (email/SMS logging).
 * Production-grade: @RetryableTopic + Dead Letter Topic.
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
