package com.microservices.pro.orderservice;

import com.microservices.pro.orderservice.events.OrderPlacedEvent;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * OrderService — Session 4 (Circuit Breaker + Retry), extended in Session 5
 * (Bulkhead + TimeLimiter, full resilience stack), extended again in
 * Session 6 (Inventory pre-check via Feign) and Session 7 (Saga via Kafka).
 *
 * ANNOTATION ORDER IS DELIBERATE AND TAUGHT EXPLICITLY — do not reorder:
 *   @Bulkhead → @TimeLimiter → @CircuitBreaker → @Retry
 * See docs/labs/session-05-lab-3b.md for why this order matters.
 *
 * SCOPE NOTE — two independent order-creation methods coexist here on
 * purpose:
 *   - createOrderAsync() — Sessions 4-5's resilience-pattern teaching method.
 *   - createOrder()      — Session 7's actual Saga initiator, exposed via
 *                           OrderController's POST /api/orders endpoint.
 *
 * Implement the TODOs below. See docs/labs/session-04-lab-3a.md,
 * docs/labs/session-05-lab-3b.md, docs/labs/session-06-lab-4a.md, and
 * docs/labs/session-07-lab-5a.md for the full lab instructions.
 */
@Service
public class OrderService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // ── Session 4-5 ──────────────────────────────────────────────────────

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

    // ── Session 6 + Session 7: the actual Saga initiator ────────────────
    //
    // No @CircuitBreaker/@Retry/@Bulkhead/@TimeLimiter here on purpose —
    // Kafka's durable log is the Saga's resilience mechanism, not
    // Resilience4j wrapping a direct call (there IS no direct call to
    // Payment in this method).

    // TODO 9 (Session 6): call inventoryClient.checkStock(productId, quantity).
    //         If !stock.available(), return immediately with status
    //         "REJECTED" — do NOT save an Order or publish any event.
    // TODO 10 (Session 7): if stock is available — save a new Order with
    //         status PENDING (UUID.randomUUID() for the orderId).
    // TODO 11 (Session 7): publish an OrderPlacedEvent to "order-events" via
    //         kafkaTemplate.send(...).
    // TODO 12: return immediately with status "PENDING" — do not wait for
    //         the Saga to finish.
    public OrderResponse createOrder(OrderRequest request) {
        throw new UnsupportedOperationException("TODO: implement createOrder()");
    }

    // TODO 13 (Session 7): look up the Order by orderId and return its
    //         status. Throw OrderNotFoundException if not found.
    public OrderStatus getOrderStatus(String orderId) {
        throw new UnsupportedOperationException("TODO: implement getOrderStatus()");
    }
}
