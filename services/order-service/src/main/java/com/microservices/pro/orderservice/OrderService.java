package com.microservices.pro.orderservice;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * OrderService — Session 4 (Circuit Breaker + Retry), extended in Session 5
 * (Bulkhead + TimeLimiter, full resilience stack).
 *
 * ANNOTATION ORDER IS DELIBERATE AND TAUGHT EXPLICITLY — do not reorder:
 *   @Bulkhead → @TimeLimiter → @CircuitBreaker → @Retry
 * See docs/labs/session-05-lab-3b.md for why this order matters.
 *
 * Implement the TODOs below. See docs/labs/session-04-lab-3a.md and
 * docs/labs/session-05-lab-3b.md for the full lab instructions.
 */
@Service
public class OrderService {

    @Autowired
    private PaymentService paymentService;

    // TODO 1 (Session 4): annotate with @CircuitBreaker(name="paymentService", fallbackMethod="paymentFallback")
    // TODO 2 (Session 4): annotate with @Retry(name="paymentService")
    // TODO 3 (Session 5): annotate with @Bulkhead(name="paymentService", fallbackMethod="bulkheadFallback")
    // TODO 4 (Session 5): annotate with @TimeLimiter(name="paymentService", fallbackMethod="timeoutFallback")
    //         Apply all four IN THIS ORDER, top to bottom:
    //         @Bulkhead, @TimeLimiter, @CircuitBreaker, @Retry
    // TODO 5: implement the method body — wrap the payment call in
    //         CompletableFuture.supplyAsync(), call paymentService.processPayment(),
    //         and return a CONFIRMED OrderResponse with the payment's transactionId
    public CompletableFuture<OrderResponse> createOrderAsync(OrderRequest request) {
        throw new UnsupportedOperationException("TODO: implement createOrderAsync()");
    }

    // TODO 6 (Session 4): implement paymentFallback — must have the same
    //         params as createOrderAsync() PLUS Throwable as the last param.
    //         Return a CompletableFuture<OrderResponse> with status "PENDING".
    public CompletableFuture<OrderResponse> paymentFallback(OrderRequest request, Throwable ex) {
        throw new UnsupportedOperationException("TODO: implement paymentFallback()");
    }

    // TODO 7 (Session 5): implement bulkheadFallback — params must be
    //         (OrderRequest, BulkheadFullException). Return status "QUEUED".
    public CompletableFuture<OrderResponse> bulkheadFallback(OrderRequest request, BulkheadFullException ex) {
        throw new UnsupportedOperationException("TODO: implement bulkheadFallback()");
    }

    // TODO 8 (Session 5): implement timeoutFallback — params must be
    //         (OrderRequest, TimeoutException). Return type MUST be
    //         CompletableFuture<OrderResponse> (matching the annotated method).
    //         Return status "PENDING".
    public CompletableFuture<OrderResponse> timeoutFallback(OrderRequest request, TimeoutException ex) {
        throw new UnsupportedOperationException("TODO: implement timeoutFallback()");
    }
}
