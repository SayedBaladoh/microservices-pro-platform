package com.microservices.pro.productservice;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * ProductServiceParameterizedTest — Session 10, Lab 9A, Task 2.
 *
 * @ParameterizedTest refactor: replaces the pattern of writing three
 * near-identical tests for findById (found with id 1, found with id 2,
 * not found with id 999) into a single parameterized test.
 *
 * This is a SEPARATE test class — the original ProductServiceTest.java
 * (from Sessions 1/8) is not modified. Both classes run as part of
 * `mvn test -pl services/product-service`.
 *
 * Teaching point from Session 10 docx:
 *   BAD: three @Test methods that differ only in input values — maintenance
 *        nightmare (change the method signature = update 3 tests).
 *   GOOD: @ParameterizedTest + @CsvSource covers all cases in one method,
 *         adding a new case is one extra line in @CsvSource.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceParameterizedTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @ParameterizedTest(name = "findById({0}) shouldExist={2}")
    @CsvSource({
            "1, Laptop Pro, true",
            "2, Wireless Mouse, true",
            "999, N/A, false"
    })
    void findById_returnsCorrectResult(Long id, String name, boolean shouldExist) {
        if (shouldExist) {
            Product product = new Product(id, name, "desc", new BigDecimal("99.99"), "ELECTRONICS");
            when(productRepository.findById(id)).thenReturn(Optional.of(product));
        } else {
            when(productRepository.findById(id)).thenReturn(Optional.empty());
        }

        Optional<Product> result = productService.findById(id);

        assertThat(result.isPresent()).isEqualTo(shouldExist);
        if (shouldExist) {
            assertThat(result.get().getName()).isEqualTo(name);
        }
    }
}
