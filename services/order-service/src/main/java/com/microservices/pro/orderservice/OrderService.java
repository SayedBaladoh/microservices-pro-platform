package com.microservices.pro.orderservice;

import com.microservices.pro.orderservice.events.OrderPlacedEvent;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * OrderService — Session 4 (Circuit Breaker + Retry), extended in Session 5
 * (Bulkhead + TimeLimiter, full resilience stack), extended again in
 * Session 6 (Inventory pre-check via Feign) and Session 7 (Saga via Kafka).
 *
 * SCOPE NOTE — two independent order-creation methods coexist here on
 * purpose, see each one's header comment:
 *   - createOrderAsync() — Sessions 4-5's resilience-pattern teaching method.
 *   - createOrder()      — Session 7's actual Saga initiator, exposed via
 *                           OrderController's POST /api/orders endpoint.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // ── Session 4-5: resilience pattern stack on a DIRECT Payment call ──
    //
    // NOTE: This method represents the Session 4/5 resilience pattern stack
    // (Bulkhead -> TimeLimiter -> CircuitBreaker -> Retry) applied to a
    // DIRECT synchronous Payment call. It remains fully functional and
    // tested for teaching purposes (Sessions 4-5), but is NOT called by
    // OrderController after Session 7 — see createOrder() below, which
    // replaces the direct Payment call with an async Saga via Kafka.
    // Resilience patterns on direct payment calls become moot once Payment
    // is invoked via event, not via direct method call.
    //
    // ANNOTATION ORDER IS DELIBERATE AND TAUGHT EXPLICITLY — do not reorder:
    //   @Bulkhead → @TimeLimiter → @CircuitBreaker → @Retry
    // See Session 5 docx, 3.6 "Execution Order — Who Runs First?".

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

    // ── Session 4-5 fallbacks (unchanged) ────────────────────────────────

    public CompletableFuture<OrderResponse> paymentFallback(OrderRequest request, Throwable ex) {
        log.warn("Payment failed, returning PENDING order. Reason: {}", ex.getMessage());
        return CompletableFuture.completedFuture(new OrderResponse(
                "PENDING",
                "Will retry payment"
        ));
    }

    public CompletableFuture<OrderResponse> bulkheadFallback(OrderRequest request, BulkheadFullException ex) {
        log.warn("[BULKHEAD] Concurrent limit reached: {}", ex.getMessage());
        return CompletableFuture.completedFuture(new OrderResponse(
                "QUEUED",
                "System busy — your order is queued"
        ));
    }

    public CompletableFuture<OrderResponse> timeoutFallback(OrderRequest request, TimeoutException ex) {
        log.warn("[TIMEOUT] Payment exceeded the configured time limit: {}", ex.getMessage());
        return CompletableFuture.completedFuture(new OrderResponse(
                "PENDING",
                "Payment timed out — will retry asynchronously"
        ));
    }

    // ── Session 6 + Session 7: the actual Saga initiator ────────────────
    //
    // Sequence (deliberate, do not reorder):
    //   1. Synchronous Inventory pre-check via Feign (Session 6). If stock
    //      is unavailable, reject immediately — no Order is saved, no event
    //      is published. The docx is explicit that this sync pre-check
    //      COEXISTS with the Saga rather than being replaced by it: "The
    //      direct OpenFeign inventory check (Lab 4A) can coexist with the
    //      Saga. Keep it for the stock pre-check. The Saga handles the
    //      committed reservation."
    //   2. Save the Order with status PENDING (only if the pre-check passed).
    //   3. Publish OrderPlacedEvent to "order-events" — this is what
    //      actually starts the Choreography Saga (Inventory's commit-level
    //      reservation, then Payment).
    //   4. Return immediately with PENDING — no resilience pattern wraps
    //      this method; Kafka's durable log is the Saga's resilience
    //      mechanism, not Resilience4j (see Session 7 docx 3.7).
    //
    // No @CircuitBreaker/@Retry/@Bulkhead/@TimeLimiter here on purpose.

    public OrderResponse createOrder(OrderRequest request) {
        StockCheckResponse stock = inventoryClient.checkStock(request.productId(), request.quantity());
        if (!stock.available()) {
            return new OrderResponse(null, "REJECTED",
                    "Insufficient stock: only " + stock.remainingStock() + " available");
        }

        Order order = new Order(
                UUID.randomUUID().toString(),
                request.productId(),
                request.quantity(),
                request.amount(),
                OrderStatus.PENDING
        );
        orderRepository.save(order);

        kafkaTemplate.send("order-events",
                order.getOrderId(),
                new OrderPlacedEvent(
                        order.getOrderId(),
                        request.productId(),
                        request.quantity(),
                        request.amount(),
                        request.customerId()
                ));

        return new OrderResponse(order.getOrderId(), "PENDING", "Order received — processing...");
    }

    public OrderStatus getOrderStatus(String orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getStatus)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }
}
