package com.microservices.pro.orderservice;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * PaymentService — Session 4, Lab 3A.
 *
 * IMPORTANT — scope note (read before touching this in Session 6+):
 *
 * This is an in-process Spring bean stub, simulating Payment Service's
 * behavior locally inside order-service. It is intentionally NOT an HTTP
 * call to the real services/payment-service module, even though that
 * module exists in this repository from this same session onward.
 *
 * Why: making Order Service call Payment Service over the network is
 * exactly the "synchronous inter-service communication" problem that
 * OpenFeign solves — and OpenFeign is Session 6 content. Wiring a real
 * HTTP/Feign call here would be curriculum leakage (OBG-001). Sessions 4-5
 * are about the RESILIENCE PATTERNS themselves (Circuit Breaker, Retry,
 * Bulkhead, TimeLimiter) — the docx's own live-coding setup explicitly
 * says "Create a simple Payment Service that randomly fails... Order
 * Service calls it" using this exact in-process random-failure design,
 * not a real network boundary.
 *
 * When Session 6 is built, OrderService should be updated to call the real
 * Payment Service via OpenFeign (or to keep both side-by-side for
 * comparison) — that is a deliberate, separate piece of work, not an
 * oversight in this class.
 */
@Service
public class PaymentService {

    private final Random random = new Random();

    /**
     * Simulates a 50% failure rate for demo purposes (Session 4).
     * Session 5 does not change this method — TimeLimiter is demonstrated
     * by the real Payment Service's configurable delay-ms instead (see
     * RetryLogger / OrderService for how Session 5's async wrapper uses it).
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        if (random.nextInt(10) < 5) {  // 50% chance
            throw new RuntimeException("Payment Service unavailable");
        }
        return new PaymentResponse(request.amount());
    }
}
