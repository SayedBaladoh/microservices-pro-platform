package com.microservices.pro.productservice;

import java.math.BigDecimal;

/**
 * Product domain model — Session 1.
 *
 * Matches the Session 1 Lab 1 spec exactly: id, name, description, price, category.
 * Plain record for now — no JPA annotations until the Session 1 homework upgrade.
 */
public record Product(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category
) {}
