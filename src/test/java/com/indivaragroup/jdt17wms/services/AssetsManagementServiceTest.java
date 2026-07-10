package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class AssetsManagementServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private AssetsManagementService assetsManagementService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(assetsManagementService);
    }
}
