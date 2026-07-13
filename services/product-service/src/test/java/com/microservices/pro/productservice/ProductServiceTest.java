package com.microservices.pro.productservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductServiceTest.
 *
 * History: Session 1 shipped these same 4 tests against an in-memory Map.
 * Since Session 1's optional JPA homework was implemented in Session 8 (see
 * Product.java / ProductRepository.java), this test now mocks
 * ProductRepository with Mockito instead — same test names, same intent
 * (findAll empty, save+findById round-trip, findById miss, deleteById),
 * adapted to the new collaborator.
 *
 * Note: this test does NOT exercise @Cacheable/@CacheEvict behavior — that
 * requires a Spring context (cache manager, Redis) and is deferred to
 * Session 10 (Integration Testing), consistent with the Unit-Tests-only
 * pattern used throughout Sessions 1-8. See docs/labs/session-08-lab-6a.md.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findAll_returnsEmptyList_whenNoProductsExist() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<Product> products = productService.findAll();

        assertThat(products).isEmpty();
    }

    @Test
    void save_storesProduct_andFindByIdRetrievesIt() {
        Product newProduct = new Product(null, "Laptop", "15-inch laptop", new BigDecimal("999.99"), "Electronics");
        Product saved = new Product(1L, "Laptop", "15-inch laptop", new BigDecimal("999.99"), "Electronics");
        when(productRepository.save(newProduct)).thenReturn(saved);
        when(productRepository.findById(1L)).thenReturn(Optional.of(saved));

        Product result = productService.save(newProduct);
        Optional<Product> found = productService.findById(result.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop");
        assertThat(found.get().getPrice()).isEqualTo(new BigDecimal("999.99"));
    }

    @Test
    void findById_returnsEmptyOptional_forNonExistentId() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> found = productService.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteById_callsRepositoryDeleteById() {
        productService.deleteById(5L);

        verify(productRepository, times(1)).deleteById(5L);
    }
}
