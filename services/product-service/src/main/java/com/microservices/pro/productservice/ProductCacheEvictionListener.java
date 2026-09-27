package com.microservices.pro.productservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * ProductCacheEvictionListener — Session 18, Lab 13B.
 *
 * Listens for ProductChangedEvent (published by ProductCommandService) and
 * evicts stale cache entries on the Query side.
 *
 * WHY a separate listener instead of @CacheEvict on ProductCommandService?
 *   Decoupling. ProductCommandService does not know which caches exist.
 *   If a second cache is added later (e.g. a "featured products" cache),
 *   a new listener handles it — ProductCommandService is unchanged.
 *   This is the ApplicationEvent as an extension seam. See Quiz Q4.
 *
 * CRITICAL: evict BOTH keys — the individual product key AND the
 * 'all-summary' list key. Missing one leaves stale data in the Query side.
 * Same lesson as Session 8 Q4, now driven by an event instead of @CacheEvict.
 *
 * @Async note: by default this runs synchronously on ProductCommandService's
 * thread. Add @Async + @EnableAsync to decouple if eviction becomes slow.
 */
@Component
public class ProductCacheEvictionListener {

    private static final Logger log = LoggerFactory.getLogger(ProductCacheEvictionListener.class);

    private final CacheManager cacheManager;

    public ProductCacheEvictionListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventListener
    public void onProductChanged(ProductChangedEvent event) {
        log.info("[CACHE] Evicting after {} on product id={}",
                event.changeType(), event.productId());

        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict(event.productId());      // individual key
            cache.evict("all-summary");          // list key — MUST NOT be omitted
        }
    }
}
