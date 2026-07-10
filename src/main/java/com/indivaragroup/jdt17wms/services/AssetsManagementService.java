package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

@Service
public class AssetsManagementService {

    private final AssetRepository assetRepository;
    private final ExpenseRepository expenseRepository;

    public AssetsManagementService(AssetRepository assetRepository, ExpenseRepository expenseRepository) {
        this.assetRepository = assetRepository;
        this.expenseRepository = expenseRepository;
    }
}
