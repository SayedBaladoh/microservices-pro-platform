package com.microservices.pro.productservice;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * ProductCommandService — Session 18, Lab 13B.
 *
 * COMMAND side of the CQRS split. Owns all WRITE operations on products:
 *   - Enforces business invariants (price > 0, name not blank)
 *   - Persists via ProductRepository
 *   - Publishes ProductChangedEvent after every successful write
 *   - Does NOT return read-shaped data (returns Product, not a projection)
 *   - Does NOT know which caches exist — that is the Listener's concern
 *
 * Replaces ProductService for write operations. ProductService is retained
 * as a compatibility shim but write paths should migrate here.
 *
 * Note on @CacheEvict: direct @CacheEvict annotations are REMOVED from this
 * class. Cache eviction now happens via ProductCacheEvictionListener reacting
 * to ProductChangedEvent. This decouples the Command service from knowledge
 * of which caches exist — a new cache can be added by adding a new listener,
 * without touching this class.
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

    @Transactional
    public Product createProduct(String name, String description,
                                  BigDecimal price, String category) {
        validateInvariants(name, price);

        Product product = new Product(null, name, description, price, category);
        Product saved = productRepository.save(product);

        eventPublisher.publishEvent(new ProductChangedEvent(saved.getId(), "CREATED"));
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, String name, String description,
                                  BigDecimal price, String category) {
        validateInvariants(name, price);

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        existing.setName(name);
        existing.setDescription(description);
        existing.setPrice(price);
        existing.setCategory(category);

        Product saved = productRepository.save(existing);
        eventPublisher.publishEvent(new ProductChangedEvent(saved.getId(), "UPDATED"));
        return saved;
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        eventPublisher.publishEvent(new ProductChangedEvent(id, "DELETED"));
    }

    // ── Business invariants ────────────────────────────────────────────────
    // Invariants live on the Command side ONLY. The Query side reads
    // already-valid persisted state — there is nothing to validate there.
    // See Session 18 Quiz Q7.

    private void validateInvariants(String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
    }
}
