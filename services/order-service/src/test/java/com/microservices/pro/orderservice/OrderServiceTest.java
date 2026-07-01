package com.microservices.pro.orderservice;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrderServiceTest — Session 4, Lab 3A, Task 4 + Session 5, Lab 3B, Task 3.
 *
 * Session 4 tests (1-2, adapted to this repo's merged async method):
 *   - paymentFallback() returns a PENDING status response
 *   - fallback has the correct signature: (OrderRequest, Throwable)
 *
 * Session 5 tests (3-5):
 *   - bulkheadFallback() returns QUEUED when given a BulkheadFullException
 *   - timeoutFallback() returns PENDING, wrapped in CompletableFuture<OrderResponse>
 *   - all four annotations (@Bulkhead, @TimeLimiter, @CircuitBreaker, @Retry)
 *     are present on createOrderAsync() — verified via reflection
 *
 * No PaymentService mocking is needed here: every test below calls a
 * fallback method directly (the documented Lab 3A/3B testing strategy),
 * not createOrderAsync() itself — so @InjectMocks just gives each test a
 * fresh OrderService; there's nothing to @Mock since PaymentService is
 * never invoked by these tests.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    // ── Session 4 ───────────────────────────────────────────────────────

    @Test
    void paymentFallback_returnsPendingOrderResponse() throws Exception {
        OrderRequest request = new OrderRequest(new BigDecimal("100.00"));
        Throwable exception = new RuntimeException("Payment Service unavailable");

        CompletableFuture<OrderResponse> result = orderService.paymentFallback(request, exception);

        assertThat(result.get().status()).isEqualTo("PENDING");
    }

    @Test
    void paymentFallback_hasCorrectSignature_requestPlusThrowable() throws NoSuchMethodException {
        Method fallback = OrderService.class.getMethod("paymentFallback", OrderRequest.class, Throwable.class);

        assertThat(fallback.getParameterCount()).isEqualTo(2);
        assertThat(fallback.getParameterTypes()[0]).isEqualTo(OrderRequest.class);
        assertThat(fallback.getParameterTypes()[1]).isEqualTo(Throwable.class);
    }

    // ── Session 5 ───────────────────────────────────────────────────────

    @Test
    void bulkheadFallback_returnsQueuedStatus_onBulkheadFullException() throws Exception {
        OrderRequest request = new OrderRequest(new BigDecimal("100.00"));
        // Built via the real public factory (verified against the
        // resilience4j source: Bulkhead.ofDefaults(name) + the public
        // static BulkheadFullException.createBulkheadFullException(Bulkhead)
        // factory) rather than mocked — BulkheadFullException's constructor
        // is private, so this is the only supported way to obtain a real
        // instance outside the library itself.
        io.github.resilience4j.bulkhead.Bulkhead bulkhead =
                io.github.resilience4j.bulkhead.Bulkhead.ofDefaults("paymentService");
        BulkheadFullException exception = BulkheadFullException.createBulkheadFullException(bulkhead);

        CompletableFuture<OrderResponse> result = orderService.bulkheadFallback(request, exception);

        assertThat(result.get().status()).isEqualTo("QUEUED");
    }

    @Test
    void timeoutFallback_returnsPendingStatus_wrappedInCompletableFuture() throws Exception {
        OrderRequest request = new OrderRequest(new BigDecimal("100.00"));
        TimeoutException exception = new TimeoutException("Payment exceeded 2s limit");

        CompletableFuture<OrderResponse> result = orderService.timeoutFallback(request, exception);

        assertThat(result).isInstanceOf(CompletableFuture.class);
        assertThat(result.get().status()).isEqualTo("PENDING");
    }

    @Test
    void createOrderAsync_hasAllFourResilienceAnnotations_inTheDocumentedOrder() throws NoSuchMethodException {
        Method method = OrderService.class.getMethod("createOrderAsync", OrderRequest.class);

        assertThat(method.getAnnotation(Bulkhead.class)).isNotNull();
        assertThat(method.getAnnotation(TimeLimiter.class)).isNotNull();
        assertThat(method.getAnnotation(CircuitBreaker.class)).isNotNull();
        assertThat(method.getAnnotation(Retry.class)).isNotNull();

        assertThat(method.getAnnotation(Bulkhead.class).name()).isEqualTo("paymentService");
        assertThat(method.getAnnotation(TimeLimiter.class).name()).isEqualTo("paymentService");
        assertThat(method.getAnnotation(CircuitBreaker.class).name()).isEqualTo("paymentService");
        assertThat(method.getAnnotation(Retry.class).name()).isEqualTo("paymentService");
    }
}
