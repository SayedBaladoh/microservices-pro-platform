package com.microservices.pro.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductControllerTest — Session 10, Lab 9A, Task 1.
 *
 * @WebMvcTest: loads ONLY the web layer (controllers, filters, MockMvc).
 * Does NOT load: Services, Repositories, full Spring context.
 * => Fast (2 seconds), focused — tests HTTP request/response in isolation.
 *
 * @MockBean replaces the real ProductService in the Spring context with a
 * Mockito mock. Without it, @WebMvcTest fails to start because it cannot
 * find a ProductService bean (service layer is not loaded).
 *
 * Path note: this repo's ProductController maps to /api/v1/products
 * (see Session 1 — the docx live-coding example uses /api/products, but
 * the actual controller built in S1 uses the /v1 versioned path).
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product sampleProduct() {
        return new Product(1L, "Laptop Pro", "High-end laptop",
                new BigDecimal("1299.99"), "ELECTRONICS");
    }

    @Test
    void getProduct_returns200_andBody_whenProductExists() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.of(sampleProduct()));

        mockMvc.perform(get("/api/v1/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.price").value(1299.99));
    }

    @Test
    void getProduct_returns404_whenNotFound() throws Exception {
        when(productService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound());
    }
}
