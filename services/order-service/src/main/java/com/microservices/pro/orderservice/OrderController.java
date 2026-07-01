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
 * Spring MVC handles CompletableFuture<ResponseEntity<...>> transparently —
 * the client sees a normal synchronous HTTP response; the async behavior
 * (and the TimeLimiter's enforcement of it) is internal to OrderService.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<OrderResponse>> createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrderAsync(request)
                .thenApply(ResponseEntity::ok);
    }
}
