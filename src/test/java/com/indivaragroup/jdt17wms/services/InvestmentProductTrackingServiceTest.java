package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.ProductPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class InvestmentProductTrackingServiceTest {

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private ProductPriceRepository productPriceRepository;

    @InjectMocks
    private InvestmentProductTrackingService investmentProductTrackingService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(investmentProductTrackingService);
    }
}
