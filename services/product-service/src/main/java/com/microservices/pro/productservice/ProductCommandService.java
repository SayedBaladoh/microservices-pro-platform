package com.microservices.pro.productservice;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * ProductCommandService — Session 18, Lab 13B.
 * Implement the TODOs. See docs/labs/session-18-lab-13b.md.
 */
@Service
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductCommandService(ProductRepository productRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    // TODO 1: Validate invariants (price > 0, name not blank)
    // TODO 2: Save via productRepository
    // TODO 3: Publish ProductChangedEvent(id, "CREATED") via eventPublisher
    // TODO 4: Return saved product
    @Transactional
    public Product createProduct(String name, String description,
                                  BigDecimal price, String category) {
        throw new UnsupportedOperationException("TODO: implement createProduct()");
    }

    // TODO 5: Validate, find existing, update fields, save, publish "UPDATED"
    @Transactional
    public Product updateProduct(Long id, String name, String description,
                                  BigDecimal price, String category) {
        throw new UnsupportedOperationException("TODO: implement updateProduct()");
    }

    // TODO 6: Verify exists, delete, publish "DELETED"
    @Transactional
    public void deleteProduct(Long id) {
        throw new UnsupportedOperationException("TODO: implement deleteProduct()");
    }
}
