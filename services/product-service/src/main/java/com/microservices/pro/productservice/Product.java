package com.microservices.pro.productservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * Product domain model.
 *
 * Originally a plain record (Session 1) backed by an in-memory Map. This
 * is the Session 1 "Optional homework — JPA + PostgreSQL persistence"
 * upgrade, implemented now (Session 8) because Session 8's Redis caching
 * pattern (@Cacheable / @CacheEvict in front of a real repository call)
 * only makes pedagogical sense in front of an actual database query —
 * caching in front of an in-memory Map has no real "DB bottleneck" to
 * solve. See docs/labs/session-08-lab-6a.md for the full note on this.
 *
 * Same fields as the original Session 1 record: id, name, description,
 * price, category. Implements Serializable because Spring Data Redis's
 * default cache serializer needs it for cached values (see
 * docs/setup/troubleshooting.md, Session 8 section).
 */
@Entity
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    private String category;

    protected Product() {
        // required by JPA
    }

    public Product(Long id, String name, String description, BigDecimal price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

