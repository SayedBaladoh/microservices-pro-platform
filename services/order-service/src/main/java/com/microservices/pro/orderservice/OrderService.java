package com.microservices.pro.orderservice;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * This is outermost-to-innermost in actual execution: Bulkhead is checked
 * first (is there a concurrent slot?), then TimeLimiter starts its timer,
 * then CircuitBreaker decides whether to allow the call through, and Retry
 * is innermost (each retry attempt is itself subject to the CB check).
 * See Session 5 docx, 3.6 "Execution Order — Who Runs First?".
 *
 * Session 4 shipped createOrder() returning OrderResponse directly with
 * @CircuitBreaker + @Retry. Session 5 converts this same method to
 * createOrderAsync() returning CompletableFuture<OrderResponse>, because
 * @TimeLimiter requires a cancellable Future to enforce its timeout —
 * see Session 5 docx, 3.4 "Why does TimeLimiter require an async method?".
 * The synchronous createOrder() is intentionally not kept alongside it;
 * the docx's Lab 3B replaces the method, it does not add a second one.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private PaymentService paymentService;

    @Bulkhead(name = "paymentService", fallbackMethod = "bulkheadFallback")
    @TimeLimiter(name = "paymentService", fallbackMethod = "timeoutFallback")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    public CompletableFuture<OrderResponse> createOrderAsync(OrderRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            PaymentResponse payment = paymentService.processPayment(
                    new PaymentRequest(request.amount())
            );
            return new OrderResponse("CONFIRMED", payment.transactionId());
        });
    }

    // ── Fallbacks ───────────────────────────────────────────────────────
    //
    // Each fallback's signature MUST match its annotated method's params
    // exactly, plus the specific exception type as the last parameter
    // (Throwable works as a catch-all, but using the precise exception
    // type — BulkheadFullException, TimeoutException — documents intent
    // and avoids one fallback accidentally swallowing another pattern's
    // failure). See Session 4 docx Daily Quiz Q4 and Session 5's note on
    // BulkheadFullException vs Throwable.

    /**
     * Session 4 — CircuitBreaker / Retry fallback. Called when the circuit
     * is OPEN, or when all retry attempts are exhausted.
     */
    public CompletableFuture<OrderResponse> paymentFallback(OrderRequest request, Throwable ex) {
        log.warn("Payment failed, returning PENDING order. Reason: {}", ex.getMessage());
        return CompletableFuture.completedFuture(new OrderResponse(
                "PENDING",
                "Will retry payment"
        ));
    }

    /**
     * Session 5 — Bulkhead fallback. Called when the concurrent-call limit
     * is exceeded — the request never even reaches Payment Service.
     */
    public CompletableFuture<OrderResponse> bulkheadFallback(OrderRequest request, BulkheadFullException ex) {
        log.warn("[BULKHEAD] Concurrent limit reached: {}", ex.getMessage());
        return CompletableFuture.completedFuture(new OrderResponse(
                "QUEUED",
                "System busy — your order is queued"
        ));
    }

    /**
     * Session 5 — TimeLimiter fallback. Called when Payment Service takes
     * longer than timeout-duration to respond. Return type must ALSO be
     * CompletableFuture<OrderResponse> — matching the annotated method.
     */
    public CompletableFuture<OrderResponse> timeoutFallback(OrderRequest request, TimeoutException ex) {
        log.warn("[TIMEOUT] Payment exceeded the configured time limit: {}", ex.getMessage());
        return CompletableFuture.completedFuture(new OrderResponse(
                "PENDING",
                "Payment timed out — will retry asynchronously"
        ));
    }
}
