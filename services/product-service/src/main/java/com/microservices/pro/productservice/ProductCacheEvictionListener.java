package com.microservices.pro.productservice;

import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * ProductCacheEvictionListener — Session 18, Lab 13B.
 * Implement the TODO. See docs/labs/session-18-lab-13b.md.
 */
@Component
public class ProductCacheEvictionListener {

    private final CacheManager cacheManager;

    public ProductCacheEvictionListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // TODO: @EventListener — evict BOTH keys on ProductChangedEvent:
    //   cache.evict(event.productId())   — individual product key
    //   cache.evict("all-summary")       — list key (MUST NOT be omitted)
    @EventListener
    public void onProductChanged(ProductChangedEvent event) {
        throw new UnsupportedOperationException("TODO: implement onProductChanged()");
    }
}
