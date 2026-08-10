package com.microservices.pro.productservice;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProductServiceParameterizedTest — Session 10, Lab 9A, Task 2.
 *
 * Refactor Session 1's three near-identical findById tests into a
 * single @ParameterizedTest.
 *
 * Implement the TODO below. See docs/labs/session-10-lab-9a.md.
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
        // TODO: wire the mock so productRepository.findById(id) returns
        //       an Optional.of(product) when shouldExist=true, Optional.empty()
        //       when shouldExist=false, then assert the result.
        throw new UnsupportedOperationException("TODO: implement this parameterized test");
    }
}
