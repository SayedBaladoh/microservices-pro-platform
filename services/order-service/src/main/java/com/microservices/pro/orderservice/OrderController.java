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
 * Implement the TODOs below. See docs/labs/session-07-lab-5a.md.
 *
 * createOrderAsync() (Sessions 4-5) is NOT exposed through any endpoint
 * here — it exists for lab/test purposes only on OrderService itself.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // TODO 1: POST /api/orders → call orderService.createOrder(request)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        throw new UnsupportedOperationException("TODO: implement createOrder()");
    }

    // TODO 2: GET /api/orders/{orderId}/status → call orderService.getOrderStatus(orderId)
    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatus> getStatus(@PathVariable String orderId) {
        throw new UnsupportedOperationException("TODO: implement getStatus()");
    }
}
