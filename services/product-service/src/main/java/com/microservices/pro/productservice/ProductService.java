package com.microservices.pro.productservice;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ProductService.
 *
 * History:
 *   Session 1 — in-memory Map<Long, Product> store (findAll, findById,
 *               save, deleteById TODOs).
 *   Session 1 homework (implemented Session 8) — replaced the in-memory
 *               Map with a real ProductRepository (JPA + PostgreSQL). See
 *               Product.java and docs/labs/session-08-lab-6a.md for why
 *               this was deferred until now.
 *   Session 8 — added @Cacheable / @CacheEvict (Redis) in front of the
 *               repository calls.
 *
 * Cache invalidation note (a documented trap — see Session 8 docx,
 * S08-Q04): evicting the individual product key alone leaves the cached
 * "all products" list stale. Every write path below evicts BOTH.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = "products", key = "'all'")
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @CacheEvict(value = "products", key = "#result.id")
    public Product save(Product product) {
        Product saved = productRepository.save(product);
        evictAllProductsCache();
        return saved;
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteById(Long id) {
        productRepository.deleteById(id);
        evictAllProductsCache();
    }

    @CacheEvict(value = "products", key = "'all'")
    public void evictAllProductsCache() {
        // Called internally on any write operation — evicts the cached
        // "all products" list so it doesn't go stale after a save/delete.
    }
}
