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

/**
 * ProductServiceIntegrationTest — Session 10, Lab 9A, Task 3.
 *
 * Full-context integration test using a real PostgreSQL container.
 *
 * Implement the TODOs below. See docs/labs/session-10-lab-9a.md.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductServiceIntegrationTest {

    // TODO 1: Declare the static PostgreSQLContainer field.
    //         @Container + static = container starts once for all tests.
    //         Use image "postgres:16-alpine".
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("productdb_test")
                    .withUsername("test")
                    .withPassword("test");

    // TODO 2: Implement @DynamicPropertySource (must be static):
    //         registry.add("spring.datasource.url", postgres::getJdbcUrl);
    //         registry.add("spring.datasource.username", postgres::getUsername);
    //         registry.add("spring.datasource.password", postgres::getPassword);
    //         registry.add("spring.cache.type", () -> "none");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        throw new UnsupportedOperationException("TODO: implement @DynamicPropertySource");
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void clearDatabase() {
        // TODO 3: productRepository.deleteAll() for test isolation
        throw new UnsupportedOperationException("TODO: implement @BeforeEach cleanup");
    }

    @Test
    void createAndRetrieve_product_withRealPostgres() {
        // TODO 4: save a product, assert the returned id is not null,
        //         then findById and assert the name matches
        throw new UnsupportedOperationException("TODO: implement this test");
    }

    @Test
    void findAll_returnsOnlyPersistedProducts() {
        // TODO 5: save 2 products, findAll(), assert size=2
        throw new UnsupportedOperationException("TODO: implement this test");
    }
}
