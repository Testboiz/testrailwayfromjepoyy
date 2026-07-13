package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.services.ActionRecommendationService;
import com.indivaragroup.jdt17wms.services.ProductRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActionRecommendationService actionRecommendationService;

    @MockBean
    private ProductRecommendationService productRecommendationService;

    @Test
    void getHealth_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/me/health"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecommendations_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/me/recommendations"))
                .andExpect(status().isOk());
    }
}
