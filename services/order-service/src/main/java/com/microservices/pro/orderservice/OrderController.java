package com.microservices.pro.orderservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OrderController.
 *
 * POST /api/orders is wired to OrderService.createOrder() — the Session 7
 * Saga initiator (Inventory pre-check via Feign, then publish
 * OrderPlacedEvent to Kafka, return PENDING immediately).
 *
 * OrderService.createOrderAsync() — the Session 4/5 resilience-pattern
 * teaching method — remains a valid, tested method on OrderService, but is
 * deliberately NOT exposed through any endpoint here. It exists for
 * Sessions 4-5's lab/test purposes only; this repo does not add a second
 * endpoint for it.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatus> getStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderStatus(orderId));
    }
}
