package com.microservices.pro.productservice;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * ProductQueryService — Session 18, Lab 13B.
 * Implement the TODOs. See docs/labs/session-18-lab-13b.md.
 */
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // TODO 1: Add @Transactional(readOnly = true) on the SERVICE method (not only the repo)
    // TODO 2: Add @Cacheable(value="products", key="'all-summary'")
    public List<ProductSummaryProjection> findAllSummaries() {
        throw new UnsupportedOperationException("TODO: implement findAllSummaries()");
    }

    // TODO 3: Add @Transactional(readOnly = true) + @Cacheable(value="products", key="#id")
    public Optional<ProductSummaryProjection> findSummaryById(Long id) {
        throw new UnsupportedOperationException("TODO: implement findSummaryById()");
    }
}
