package com.microservices.pro.productservice;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ProductQueryService — Session 18, Lab 13B.
 *
 * QUERY side of the CQRS split. Owns all READ operations on products:
 *   - Returns data shaped for the consumer (ProductSummaryProjection)
 *   - Marked @Transactional(readOnly = true) — tells the JPA provider to
 *     skip dirty-checking at flush time (a meaningful performance gain at
 *     scale, even on the same database)
 *   - @Cacheable annotations live HERE (reads benefit from caching;
 *     cache eviction is triggered by ProductChangedEvent, not this class)
 *   - Never changes state, never throws business-invariant exceptions
 *
 * The @Transactional(readOnly = true) annotation must be on the SERVICE
 * method, not only on the repository — Spring applies the proxy at the
 * service layer where @Transactional is declared. See Common Issues §5.
 */
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all-summary'")
    public List<ProductSummaryProjection> findAllSummaries() {
        return productRepository.findAllSummaries();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public Optional<ProductSummaryProjection> findSummaryById(Long id) {
        return productRepository.findSummaryById(id);
    }
}
