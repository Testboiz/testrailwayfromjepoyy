package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.request.AssetRegistrationDTO;
import com.indivaragroup.jdt17wms.dto.request.GoalSettingDTO;
import com.indivaragroup.jdt17wms.exceptions.DelistedProductException;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.services.AssetsManagementService;
import com.indivaragroup.jdt17wms.services.PnLCalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssetControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetsManagementService assetsManagementService;

    @MockBean
    private PnLCalculationService pnLCalculationService;

    @Test
    void createAsset_shouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        when(assetsManagementService.createAssetForUser(any(AssetRegistrationDTO.class)))
                .thenReturn(new Asset());

        mockMvc.perform(post("/api/v1/me/assets")
                        .contentType("application/json")
                        .content("{\"product_id\":\"" + productId + "\",\"units\":10.5,\"amount\":100.0,\"current_value\":110.0}"))
                .andExpect(status().isOk());
    }

    @Test
    void createAsset_shouldReturn400WhenFieldsAreInvalid() throws Exception {
        UUID productId = UUID.randomUUID();

        // units are negative -> invalid
        mockMvc.perform(post("/api/v1/me/assets")
                        .contentType("application/json")
                        .content("{\"product_id\":\"" + productId + "\",\"units\":-10.5,\"amount\":100.0,\"current_value\":110.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid field values"))
                .andExpect(jsonPath("$.type").value("ERR-001"))
                .andExpect(jsonPath("$.details[0].field").value("units"))
                .andExpect(jsonPath("$.details[0].reason").value("Must not be negative"));
    }

    @Test
    void createAsset_shouldReturn422WhenProductIsDelisted() throws Exception {
        UUID productId = UUID.randomUUID();
        when(assetsManagementService.createAssetForUser(any(AssetRegistrationDTO.class)))
                .thenThrow(new DelistedProductException("Can’t track delisted products"));

        mockMvc.perform(post("/api/v1/me/assets")
                        .contentType("application/json")
                        .content("{\"product_id\":\"" + productId + "\",\"units\":10.5,\"amount\":100.0,\"current_value\":110.0}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Can’t track delisted products"))
                .andExpect(jsonPath("$.type").value("ERR-004"))
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void getAssets_shouldReturnOk() throws Exception {
        when(assetsManagementService.getAssetsForUser()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/me/assets"))
                .andExpect(status().isOk());
    }

    @Test
    void getAssets_shouldReturn422WhenQuestionnaireNotCompleted() throws Exception {
        when(assetsManagementService.getAssetsForUser())
                .thenThrow(new MissingRiskProfileException("Risk Profiler Assessment Required"));

        mockMvc.perform(get("/api/v1/me/assets"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Risk Profiler Assessment Required"))
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void getTransactionLogs_shouldReturnOk() throws Exception {
        when(assetsManagementService.getTransactionLogsForUser()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/me/assets/transaction-logs"))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactionLogs_shouldReturn422WhenQuestionnaireNotCompleted() throws Exception {
        when(assetsManagementService.getTransactionLogsForUser())
                .thenThrow(new MissingRiskProfileException("Risk Profiler Assessment Required"));

        mockMvc.perform(get("/api/v1/me/assets/transaction-logs"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Risk Profiler Assessment Required"))
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    void updateAsset_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        when(assetsManagementService.updateAssetForUser(any(UUID.class), any(GoalSettingDTO.class)))
                .thenReturn(new Asset());

        mockMvc.perform(put("/api/v1/me/assets/" + id)
                        .contentType("application/json")
                        .content("{\"goalId\":\"" + goalId + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAsset_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/me/assets/" + id))
                .andExpect(status().isNoContent());
    }
}
