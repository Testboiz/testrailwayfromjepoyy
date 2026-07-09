package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class AssetController {

    @PostMapping("/api/v1/me/assets")
    public void createAsset() {
    }

    @GetMapping({"/api/v1/me/assets", "/ap/v1/me/assets"})
    public void getAssets() {
    }

    @GetMapping("/api/v1/me/assets/transactions-logs")
    public void getTransactionLogs() {
    }

    @PutMapping({"/api/v1/me/assets/{id}", "/me/assets/{id}"})
    public void updateAsset(@PathVariable UUID id) {
    }

    @DeleteMapping({"/api/v1/me/assets/{id}", "/me/assets/{id}"})
    public void deleteAsset(@PathVariable UUID id) {
    }
}
