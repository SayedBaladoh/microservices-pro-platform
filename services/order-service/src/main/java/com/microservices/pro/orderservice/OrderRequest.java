package com.microservices.pro.orderservice;

import java.math.BigDecimal;

/**
 * OrderRequest.
 *
 * Session 4 shipped this as (amount) only — used by createOrderAsync(),
 * which remains unchanged. Sessions 6-7 add productId, quantity, and
 * customerId, needed for the Inventory stock check (Feign, Session 6) and
 * the OrderPlacedEvent published by createOrder() (Session 7). The
 * single-argument constructor below keeps every Session 4/5 call site and
 * test compiling unchanged, with the new fields simply null/0 for those
 * call sites (they were never about checking real inventory or publishing
 * a real Saga event in the first place — see the scope note in
 * OrderService.java).
 */
public record OrderRequest(String productId, int quantity, BigDecimal amount, String customerId) {

    public OrderRequest(BigDecimal amount) {
        this(null, 0, amount, null);
    }
}
