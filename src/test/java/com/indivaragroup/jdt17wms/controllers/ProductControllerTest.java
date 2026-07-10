package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.services.ProductManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductManagementService productManagementService;

    @Test
    void getAllProducts_shouldReturnOk() throws Exception {
        Page<Product> expectedPage = new PageImpl<>(List.of());
        when(productManagementService.getAllProducts(any(Pageable.class))).thenReturn(expectedPage);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    void updateProduct_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/products/" + id))
                .andExpect(status().isOk());
    }
}

