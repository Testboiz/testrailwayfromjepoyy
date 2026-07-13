package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.request.AssetRegistrationDTO;
import com.indivaragroup.jdt17wms.dto.request.GoalSettingDTO;
import com.indivaragroup.jdt17wms.dto.response.AssetsPnLResponseDTO;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.TransactionHistory;
import com.indivaragroup.jdt17wms.services.AssetsManagementService;
import com.indivaragroup.jdt17wms.services.PnLCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AssetController {

    private final AssetsManagementService assetsManagementService;
    private final PnLCalculationService pnLCalculationService;

  public AssetController(AssetsManagementService assetsManagementService, 
                         PnLCalculationService pnLCalculationService) {
        this.assetsManagementService = assetsManagementService;
        this.pnLCalculationService = pnLCalculationService;
  }

    @PostMapping("/api/v1/me/assets")
    public Asset createAsset(@Valid @RequestBody AssetRegistrationDTO assetRegistrationDTO) {
        return assetsManagementService.createAssetForUser(assetRegistrationDTO);
    }

    @GetMapping("/api/v1/me/assets")
    public List<Asset> getAssets() {
        return assetsManagementService.getAssetsForUser();
    }

    @GetMapping("/api/v1/me/assets/pnl")
    public List<AssetsPnLResponseDTO> getAssetsPnL() {
        return pnLCalculationService.computePnLForAllAssets();
    }

    @GetMapping("/api/v1/me/assets/transaction-logs")
    public List<TransactionHistory> getTransactionLogs() {
        return assetsManagementService.getTransactionLogsForUser();
    }

    @PutMapping("/api/v1/me/assets/{id}")
    public Asset updateAsset(@PathVariable UUID id, @RequestBody GoalSettingDTO goalSettingDTO) {
        return assetsManagementService.updateAssetForUser(id, goalSettingDTO);
    }

    @DeleteMapping("/api/v1/me/assets/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable UUID id) {
        assetsManagementService.deleteAssetForUser(id);
    }
}
