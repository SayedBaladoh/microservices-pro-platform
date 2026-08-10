package com.microservices.pro.productservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductServiceIntegrationTest — Session 10, Lab 9A, Task 3.
 *
 * @SpringBootTest(RANDOM_PORT) + @Testcontainers: the full application
 * context starts against a REAL PostgreSQL container.
 *
 * Key decisions from the ⚖ ENGINEERING DECISION slide (Session 10):
 *   - @Container is STATIC → container starts once for all @Test methods
 *     in this class (one Docker startup = one container lifecycle).
 *     If it were instance-scoped, the container would start/stop for
 *     every @Test, multiplying startup time by the test count.
 *   - @DynamicPropertySource must ALSO be static → it is called once
 *     before the Spring context starts to inject the container's
 *     dynamic JDBC URL (host+port assigned by Docker at runtime).
 *   - @BeforeEach deleteAll() for data isolation → avoids @DirtiesContext
 *     (which would restart the Spring context between tests = slow).
 *
 * Requires Docker Desktop running. If Docker is not available, the test
 * is skipped by TestContainers automatically (does not fail the build).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("productdb_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Disable Redis auto-configuration for this integration test
        // (we are testing the JPA/repository layer, not the cache).
        registry.add("spring.cache.type", () -> "none");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void clearDatabase() {
        productRepository.deleteAll();
    }

    @Test
    void createAndRetrieve_product_withRealPostgres() {
        Product product = new Product(null, "Laptop Pro", "High-end laptop",
                new BigDecimal("1299.99"), "ELECTRONICS");

        Product saved = productService.save(product);

        assertThat(saved.getId()).isNotNull();   // id assigned by real PostgreSQL

        Optional<Product> found = productService.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop Pro");
    }

    @Test
    void findAll_returnsOnlyPersistedProducts() {
        productService.save(new Product(null, "Mouse", "Wireless", new BigDecimal("29.99"), "ACCESSORIES"));
        productService.save(new Product(null, "Keyboard", "Mechanical", new BigDecimal("89.99"), "ACCESSORIES"));

        List<Product> all = productService.findAll();

        assertThat(all).hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Mouse", "Keyboard");
    }
}
