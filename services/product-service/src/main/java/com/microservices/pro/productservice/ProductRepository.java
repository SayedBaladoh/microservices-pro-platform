package com.microservices.pro.productservice;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ProductRepository — Session 1 homework, implemented Session 8.
 *
 * No custom methods needed yet — JpaRepository provides findAll(),
 * findById(), save(), and deleteById() out of the box, which is all
 * ProductService currently needs.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
