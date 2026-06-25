package com.microservices.pro.productservice;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProductService — Session 1, Lab 1.
 *
 * In-memory store (no DB yet — see Session 1 homework for the JPA + PostgreSQL
 * upgrade). Implements the 5 TODOs from the original lab spec:
 *   1. Map<Long, Product> as the in-memory store
 *   2. findAll()
 *   3. findById(Long id)
 *   4. save(Product product)
 *   5. deleteById(Long id)
 */
@Service
public class ProductService {

    // TODO 1 (solved in reference): in-memory store
    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    // TODO 2 (solved in reference): findAll()
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    // TODO 3 (solved in reference): findById(Long id)
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    // TODO 4 (solved in reference): save(Product product)
    public Product save(Product product) {
        Long id = product.id() != null ? product.id() : idSequence.incrementAndGet();
        Product saved = new Product(
                id,
                product.name(),
                product.description(),
                product.price(),
                product.category()
        );
        store.put(id, saved);
        return saved;
    }

    // TODO 5 (solved in reference): deleteById(Long id)
    public void deleteById(Long id) {
        store.remove(id);
    }
}
