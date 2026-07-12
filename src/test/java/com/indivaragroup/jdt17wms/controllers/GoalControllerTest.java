package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.response.GoalDTO;
import com.indivaragroup.jdt17wms.services.GoalsManagementService;
import com.indivaragroup.jdt17wms.services.GoalsProjectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalsManagementService goalsManagementService;

    @MockBean
    private GoalsProjectionService goalsProjectionService;

    @Test
    void getGoals_shouldReturnOk() throws Exception {
        GoalDTO goalDto = GoalDTO.builder()
                .id(UUID.randomUUID())
                .name("Retirement Fund")
                .targetAmount(new java.math.BigDecimal("500000.00"))
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();
        when(goalsManagementService.getGoalsForUser()).thenReturn(java.util.List.of(goalDto));

        mockMvc.perform(get("/api/v1/me/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Retirement Fund"))
                .andExpect(jsonPath("$[0].target_amount").value(500000.00))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void createGoal_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/me/goals"))
                .andExpect(status().isOk());
    }

    @Test
    void updateGoal_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/me/goals/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void deleteGoal_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/me/goals/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void getGoalProjections_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/me/goals/projections"))
                .andExpect(status().isOk());
    }
}
