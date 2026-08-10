package com.microservices.pro.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ProductControllerTest — Session 10, Lab 9A, Task 1.
 *
 * @WebMvcTest loads ONLY the web layer — no service beans, no repositories,
 * no full context. Fast and focused.
 *
 * Implement the TODOs below. See docs/labs/session-10-lab-9a.md.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // TODO 1: Replace ProductService with a @MockBean so @WebMvcTest can start.
    //         Without this, the context fails: "No bean of type ProductService found."
    @MockBean
    private ProductService productService;

    @Test
    void getProduct_returns200_andBody_whenProductExists() throws Exception {
        // TODO 2: when(productService.findById(1L)).thenReturn(Optional.of(...))
        // TODO 3: mockMvc.perform(get("/api/v1/products/1").accept(...))
        //             .andExpect(status().isOk())
        //             .andExpect(jsonPath("$.name").value("..."))
        throw new UnsupportedOperationException("TODO: implement this test");
    }

    @Test
    void getProduct_returns404_whenNotFound() throws Exception {
        // TODO 4: when(productService.findById(99L)).thenReturn(Optional.empty())
        // TODO 5: mockMvc.perform(get("/api/v1/products/99"))
        //             .andExpect(status().isNotFound())
        throw new UnsupportedOperationException("TODO: implement this test");
    }
}
