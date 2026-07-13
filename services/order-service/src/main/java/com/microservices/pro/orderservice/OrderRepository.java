package com.microservices.pro.orderservice;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * OrderRepository — Session 7.
 */
public interface OrderRepository extends JpaRepository<Order, String> {
}
