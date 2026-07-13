package com.microservices.pro.orderservice;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 * Order — Session 7.
 *
 * JPA entity (the docx uses orderRepository.save(order) / findById(...)
 * directly — this is Session 7's own original content, not a deferred
 * homework upgrade like Product.java's JPA conversion in Session 8).
 *
 * orderId is a String (UUID), assigned by the application — not an
 * auto-generated database identity — matching
 * UUID.randomUUID().toString() in OrderService.createOrder().
 */
@Entity
public class Order {

    @Id
    private String orderId;

    private String productId;
    private int quantity;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected Order() {
        // required by JPA
    }

    public Order(String orderId, String productId, int quantity, BigDecimal amount, OrderStatus status) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
