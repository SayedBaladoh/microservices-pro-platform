package com.microservices.pro.productservice;

/**
 * ProductSummaryProjection — Session 18, CQRS Read Model.
 *
 * Spring Data interface-based projection: Spring generates the implementation
 * from a JPQL query defined in ProductRepository. Shaped for what the
 * CONSUMER needs (summary view), not what the writer stores.
 *
 * Note: categoryName is joined in — the Write Model (Product entity) stores
 * only a category string; this projection exposes it under a name suited
 * for display, without changing the entity.
 *
 * This is explicitly NOT Event Sourcing, NOT a separate physical database,
 * NOT Axon Framework — same database, same table, different view. See
 * Session 18 §3.4 Design Choice for when a heavier CQRS setup is warranted.
 */
public interface ProductSummaryProjection {
    Long getId();
    String getName();
    String getCategory();
    java.math.BigDecimal getPrice();
}
