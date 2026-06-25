package com.microservices.pro.productservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductServiceTest — Session 1, Lab 1, Task 3.
 *
 * Matches the original lab spec's required + bonus tests:
 *   Test 1 (required): findAll() returns empty list when no products exist
 *   Test 2 (required): save() stores a product and findById() retrieves it
 *   Test 3 (required): findById() returns empty Optional for non-existent id
 *   Test 4 (bonus, included here to round out the 4-test scope for this repo):
 *           deleteById() removes the product
 *
 * ProductService has no external collaborators to mock (it owns its own
 * in-memory store), so @InjectMocks here just gives each test a fresh instance —
 * there is nothing to @Mock.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Test
    void findAll_returnsEmptyList_whenNoProductsExist() {
        List<Product> products = productService.findAll();

        assertThat(products).isEmpty();
    }

    @Test
    void save_storesProduct_andFindByIdRetrievesIt() {
        Product newProduct = new Product(null, "Laptop", "15-inch laptop", new BigDecimal("999.99"), "Electronics");

        Product saved = productService.save(newProduct);
        Optional<Product> found = productService.findById(saved.id());

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Laptop");
        assertThat(found.get().price()).isEqualTo(new BigDecimal("999.99"));
    }

    @Test
    void findById_returnsEmptyOptional_forNonExistentId() {
        Optional<Product> found = productService.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteById_removesTheProduct() {
        Product newProduct = new Product(null, "Mouse", "Wireless mouse", new BigDecimal("19.99"), "Electronics");
        Product saved = productService.save(newProduct);

        productService.deleteById(saved.id());

        assertThat(productService.findById(saved.id())).isEmpty();
    }
}
