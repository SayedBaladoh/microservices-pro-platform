package com.microservices.pro.orderservice;

import java.math.BigDecimal;

/**
 * OrderRequest — Session 4.
 */
public record OrderRequest(BigDecimal amount) {}
