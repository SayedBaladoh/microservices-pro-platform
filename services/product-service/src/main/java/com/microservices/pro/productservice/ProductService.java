package com.microservices.pro.productservice;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ProductService.
 *
 * History:
 *   Session 1 — in-memory Map<Long, Product> store.
 *   Session 1 homework (already wired into this skeleton) — replaced with
 *               a real ProductRepository (JPA + PostgreSQL).
 *   Session 8 — add @Cacheable / @CacheEvict (Redis) in front of the
 *               repository calls below. This is the actual Session 8 lab
 *               TODO — implement the annotations and (for save/deleteById)
 *               the cache-eviction logic.
 *
 * Implement the TODOs below. See docs/labs/session-08-lab-6a.md.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // TODO 1: annotate with @Cacheable(value="products", key="'all'")
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // TODO 2: annotate with @Cacheable(value="products", key="#id")
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    // TODO 3: annotate with @CacheEvict(value="products", key="#result.id")
    //         Also call evictAllProductsCache() — saving a product must also
    //         invalidate the cached "all products" list, or it goes stale
    //         (a documented trap — see Session 8 docx, S08-Q04).
    public Product save(Product product) {
        Product saved = productRepository.save(product);
        evictAllProductsCache();
        return saved;
    }

    // TODO 4: annotate with @CacheEvict(value="products", key="#id")
    //         Also call evictAllProductsCache() — same reasoning as save().
    public void deleteById(Long id) {
        productRepository.deleteById(id);
        evictAllProductsCache();
    }

    // TODO 5: annotate with @CacheEvict(value="products", key="'all'")
    public void evictAllProductsCache() {
        // Called internally on any write operation.
    }
}
