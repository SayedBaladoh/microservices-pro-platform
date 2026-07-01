package com.microservices.pro.orderservice;

import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RetryLogger — Session 4, bonus ("Add logging to observe retry attempts").
 *
 * Attaches an event listener to the "paymentService" Retry instance so
 * each attempt (and its eventual success/exhaustion) is visible in logs
 * with the [RETRY] prefix — useful for demoing exponential backoff timing
 * live (500ms → 1000ms → 2000ms gaps between log lines).
 */
@Component
public class RetryLogger {

    private static final Logger log = LoggerFactory.getLogger(RetryLogger.class);

    @Autowired
    private RetryRegistry retryRegistry;

    @PostConstruct
    public void attachRetryListeners() {
        retryRegistry.retry("paymentService").getEventPublisher()
                .onRetry(event ->
                        log.warn("[RETRY] Attempt #{} for paymentService — {}",
                                event.getNumberOfRetryAttempts(),
                                event.getLastThrowable().getMessage()))
                .onSuccess(event ->
                        log.info("[RETRY] Succeeded after {} attempt(s)",
                                event.getNumberOfRetryAttempts()));
    }
}
