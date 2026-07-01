package com.microservices.pro.orderservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * OrderController — Session 4, extended in Session 5.
 *
 * Implement the TODO below. See docs/labs/session-05-lab-3b.md — Spring MVC
 * handles CompletableFuture<ResponseEntity<...>> transparently; do NOT call
 * .get() or .join() here, that would defeat the purpose of the async wrapper.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // TODO: POST /api/orders → call orderService.createOrderAsync(request)
    //       and map the result to ResponseEntity.ok(...) via .thenApply()
    @PostMapping
    public CompletableFuture<ResponseEntity<OrderResponse>> createOrder(@RequestBody OrderRequest request) {
        throw new UnsupportedOperationException("TODO: implement createOrder()");
    }
}
