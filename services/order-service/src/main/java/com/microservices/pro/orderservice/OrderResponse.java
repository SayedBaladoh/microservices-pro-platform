package com.microservices.pro.orderservice;

/**
 * OrderResponse.
 *
 * Session 4 shipped this as (status, message) — used by createOrderAsync()'s
 * fallbacks (paymentFallback, bulkheadFallback, timeoutFallback), which
 * remain unchanged. Session 7 adds orderId, since the Saga's createOrder()
 * returns the new order's id immediately alongside its PENDING status (see
 * Session 7 docx: "return new OrderResponse(order.getOrderId(),
 * OrderStatus.PENDING, ...)"). The two-argument constructor below keeps
 * every Session 4/5 call site compiling unchanged, with orderId simply null
 * for those responses (they were never about a persisted, queryable Order
 * in the first place — see the scope note on createOrderAsync() in
 * OrderService.java).
 */
public record OrderResponse(String orderId, String status, String message) {

    public OrderResponse(String status, String message) {
        this(null, status, message);
    }
}
