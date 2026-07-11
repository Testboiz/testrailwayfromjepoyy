package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.request.AssetRegistrationDTO;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.TransactionHistory;
import com.indivaragroup.jdt17wms.services.AssetsManagementService;
import com.indivaragroup.jdt17wms.services.InvestmentProductTrackingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AssetController {

    private final AssetsManagementService assetsManagementService;
    private final InvestmentProductTrackingService investmentProductTrackingService;

    public AssetController(AssetsManagementService assetsManagementService,
                           InvestmentProductTrackingService investmentProductTrackingService) {
        this.assetsManagementService = assetsManagementService;
        this.investmentProductTrackingService = investmentProductTrackingService;
    }

    @PostMapping("/api/v1/me/assets")
    public Asset createAsset(@Valid @RequestBody AssetRegistrationDTO assetRegistrationDTO) {
        return assetsManagementService.createAssetForUser(assetRegistrationDTO);
    }

    @GetMapping({"/api/v1/me/assets"})
    public List<Asset> getAssets() {
        return assetsManagementService.getAssetsForUser();
    }

    @GetMapping({"/api/v1/me/assets/transactions-logs", "/api/v1/me/assets/transaction-logs"})
    public List<TransactionHistory> getTransactionLogs() {
        return assetsManagementService.getTransactionLogsForUser();
    }

    @PutMapping({"/api/v1/me/assets/{id}"})
    public void updateAsset(@PathVariable UUID id) {
    }

    @DeleteMapping({"/api/v1/me/assets/{id}"})
    public void deleteAsset(@PathVariable UUID id) {
    }
}
