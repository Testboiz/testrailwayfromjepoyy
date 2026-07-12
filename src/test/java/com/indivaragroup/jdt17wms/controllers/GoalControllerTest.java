package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.request.GoalRegistrationDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.indivaragroup.jdt17wms.dto.request.GoalEditingDTO;

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
        GoalDTO goalDto = GoalDTO.builder()
                .id(UUID.randomUUID())
                .name("Retirement Fund")
                .targetAmount(new java.math.BigDecimal("500000.00"))
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();
        when(goalsManagementService.createGoalForUser(any(GoalRegistrationDTO.class))).thenReturn(goalDto);

        mockMvc.perform(post("/api/v1/me/goals")
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"type\":\"retirement\",\"target_amount\":500000.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Retirement Fund"))
                .andExpect(jsonPath("$.target_amount").value(500000.00))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void createGoal_shouldReturn400WhenFieldsAreInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/me/goals")
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"type\":\"retirement\",\"target_amount\":-100.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid field values"))
                .andExpect(jsonPath("$.type").value("ERR-001"))
                .andExpect(jsonPath("$.details[0].field").value("target_amount"))
                .andExpect(jsonPath("$.details[0].reason").value("Must not be negative"));
    }

    @Test
    void createGoal_shouldReturn422WhenMultiplePriority() throws Exception {
        when(goalsManagementService.createGoalForUser(any(GoalRegistrationDTO.class)))
                .thenThrow(new com.indivaragroup.jdt17wms.exceptions.DuplicatePriorityGoalException("Can’t set more than 1 priority"));

        mockMvc.perform(post("/api/v1/me/goals")
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"type\":\"retirement\",\"target_amount\":500000.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Can’t set more than 1 priority"))
                .andExpect(jsonPath("$.type").value("ERR-002"))
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void updateGoal_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        GoalDTO goalDto = GoalDTO.builder()
                .id(id)
                .name("Retirement Fund")
                .targetAmount(new java.math.BigDecimal("500000.00"))
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();
        when(goalsManagementService.updateGoalForUser(any(UUID.class), any(GoalEditingDTO.class))).thenReturn(goalDto);

        mockMvc.perform(put("/api/v1/me/goals/" + id)
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"target_amount\":500000.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Retirement Fund"))
                .andExpect(jsonPath("$.target_amount").value(500000.00))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateGoal_shouldReturn400WhenFieldsAreInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/me/goals/" + id)
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"target_amount\":-100.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid field values"))
                .andExpect(jsonPath("$.type").value("ERR-001"))
                .andExpect(jsonPath("$.details[0].field").value("target_amount"))
                .andExpect(jsonPath("$.details[0].reason").value("Must not be negative"));
    }

    @Test
    void updateGoal_shouldReturn422WhenMultiplePriority() throws Exception {
        UUID id = UUID.randomUUID();
        when(goalsManagementService.updateGoalForUser(any(UUID.class), any(GoalEditingDTO.class)))
                .thenThrow(new com.indivaragroup.jdt17wms.exceptions.DuplicatePriorityGoalException("Can’t set more than 1 priority"));

        mockMvc.perform(put("/api/v1/me/goals/" + id)
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"target_amount\":500000.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Can’t set more than 1 priority"))
                .andExpect(jsonPath("$.type").value("ERR-002"))
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void updateGoal_shouldReturn422WhenInsufficientIncome() throws Exception {
        UUID id = UUID.randomUUID();
        when(goalsManagementService.updateGoalForUser(any(UUID.class), any(GoalEditingDTO.class)))
                .thenThrow(new com.indivaragroup.jdt17wms.exceptions.InsufficientIncomeException("Can’t set more allocation than income"));

        mockMvc.perform(put("/api/v1/me/goals/" + id)
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"target_amount\":500000.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Can’t set more allocation than income"))
                .andExpect(jsonPath("$.type").value("ERR-003"))
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void updateGoal_shouldReturn404WhenGoalNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(goalsManagementService.updateGoalForUser(any(UUID.class), any(GoalEditingDTO.class)))
                .thenThrow(new com.indivaragroup.jdt17wms.exceptions.NotFoundException("No valid item with the ID"));

        mockMvc.perform(put("/api/v1/me/goals/" + id)
                        .contentType("application/json")
                        .content("{\"name\":\"Retirement Fund\",\"target_amount\":500000.0,\"monthly_contribution\":1000.0,\"target_date\":\"2040-01-01\",\"is_priority\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No valid item with the ID"))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteGoal_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/me/goals/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteGoal_shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new com.indivaragroup.jdt17wms.exceptions.NotFoundException("No valid item with the ID"))
                .when(goalsManagementService).deleteGoalForUser(any(UUID.class));

        mockMvc.perform(delete("/api/v1/me/goals/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No valid item with the ID"))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteGoal_shouldReturn422WhenQuestionnaireNotCompleted() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException("Risk Profiler Assessment Required"))
                .when(goalsManagementService).deleteGoalForUser(any(UUID.class));

        mockMvc.perform(delete("/api/v1/me/goals/" + id))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Risk Profiler Assessment Required"))
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void getGoalProjections_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/me/goals/projections"))
                .andExpect(status().isOk());
    }
}
