package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.response.AssetsPnLResponseDTO;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.TransactionHistory;
import com.indivaragroup.jdt17wms.models.enums.TransactionAction;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PnLCalculationServiceTest {

    @Mock
    private AssetRepository assetRepository;
    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PnLCalculationService pnLCalculationService;

    private Asset asset;
    private Product product;
    private final UUID assetId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(productId)
                .name("Test Stock")
                .type("stock")
                .currentPrice(BigDecimal.valueOf(5500.00))
                .build();

        asset = Asset.builder()
                .id(assetId)
                .productId(productId)
                .units(BigDecimal.valueOf(100.00))
                .amount(BigDecimal.valueOf(500000.00)) // 100 * 5000
                .build();
    }

    @Test
    void testComputePnL_SingleBuyOnly_PositivePnL() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        TransactionHistory buy = TransactionHistory.builder()
                .action(TransactionAction.BUY)
                .units(BigDecimal.valueOf(100))
                .pricePerUnit(BigDecimal.valueOf(5000))
                .totalAmount(BigDecimal.valueOf(500000))
                .build();

        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.BUY))
                .thenReturn(List.of(buy));
        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.SELL))
                .thenReturn(List.of());

        AssetsPnLResponseDTO result = pnLCalculationService.computePnLForAsset(asset);

        // Avg Price: 5000
        assertEquals(BigDecimal.valueOf(5000).setScale(4, RoundingMode.HALF_UP), result.getAvgPrice());
        
        // Remaining units: 100
        assertEquals(BigDecimal.valueOf(100.0), result.getUnits());
        
        // Current value: 100 * 5500 = 550000
        assertEquals(BigDecimal.valueOf(550000).setScale(2, RoundingMode.HALF_UP), result.getCurrentValue());
        
        // Potential PnL: (5500 - 5000) * 100 = 50000
        assertEquals(BigDecimal.valueOf(50000).setScale(2, RoundingMode.HALF_UP), result.getPotentialPnL());
        
        // Potential PnL %: 500 / 5000 * 100 = 10%
        assertEquals(BigDecimal.valueOf(10.00).setScale(2, RoundingMode.HALF_UP), result.getPotentialPnLPercent());
        
        // Realized PnL: 0
        assertEquals(BigDecimal.ZERO, result.getRealizedPnL());
        assertEquals(BigDecimal.ZERO, result.getRealizedPnLPercent());
    }

    @Test
    void testComputePnL_MultipleBuys_WeightedAverage() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // Buy 1: 100 units @ 5000
        TransactionHistory buy1 = TransactionHistory.builder()
                .action(TransactionAction.BUY)
                .units(BigDecimal.valueOf(100))
                .pricePerUnit(BigDecimal.valueOf(5000))
                .totalAmount(BigDecimal.valueOf(500000))
                .build();
                
        // Buy 2: 50 units @ 5200
        TransactionHistory buy2 = TransactionHistory.builder()
                .action(TransactionAction.BUY)
                .units(BigDecimal.valueOf(50))
                .pricePerUnit(BigDecimal.valueOf(5200))
                .totalAmount(BigDecimal.valueOf(260000))
                .build();

        asset.setUnits(BigDecimal.valueOf(150));

        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.BUY))
                .thenReturn(List.of(buy1, buy2));
        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.SELL))
                .thenReturn(List.of());

        AssetsPnLResponseDTO result = pnLCalculationService.computePnLForAsset(asset);

        // Avg Price: (500000 + 260000) / 150 = 5066.6667
        assertEquals(BigDecimal.valueOf(5066.6667).setScale(4, RoundingMode.HALF_UP), result.getAvgPrice());
    }

    @Test
    void testComputePnL_WithPartialSell_RealizedAndPotentialPnL() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // Buy 100 @ 5000
        TransactionHistory buy = TransactionHistory.builder()
                .action(TransactionAction.BUY)
                .units(BigDecimal.valueOf(100))
                .pricePerUnit(BigDecimal.valueOf(5000))
                .totalAmount(BigDecimal.valueOf(500000))
                .build();

        // Sell 40 @ 5300 (Realized gain: 40 * (5300 - 5000) = 12000)
        TransactionHistory sell = TransactionHistory.builder()
                .action(TransactionAction.SELL)
                .units(BigDecimal.valueOf(40))
                .pricePerUnit(BigDecimal.valueOf(5300))
                .totalAmount(BigDecimal.valueOf(212000))
                .build();

        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.BUY))
                .thenReturn(List.of(buy));
        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.SELL))
                .thenReturn(List.of(sell));

        AssetsPnLResponseDTO result = pnLCalculationService.computePnLForAsset(asset);

        // Remaining units: 100 - 40 = 60
        assertEquals(BigDecimal.valueOf(60.0), result.getUnits());

        // Potential PnL on remaining 60 units: 60 * (5500 - 5000) = 30000
        assertEquals(BigDecimal.valueOf(30000).setScale(2, RoundingMode.HALF_UP), result.getPotentialPnL());

        // Realized PnL: 212000 - (40 * 5000) = 12000
        assertEquals(BigDecimal.valueOf(12000).setScale(2, RoundingMode.HALF_UP), result.getRealizedPnL());

        // Realized PnL %: 12000 / 200000 * 100 = 6%
        assertEquals(BigDecimal.valueOf(6.00).setScale(2, RoundingMode.HALF_UP), result.getRealizedPnLPercent());
    }

    @Test
    void testComputePnL_NoBuyTransactions_FallbackToAssetAmount() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // No transactions exist (legacy data)
        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.BUY))
                .thenReturn(List.of());
        when(transactionHistoryRepository.findAllByAssetIdAndActionOrderByTransactionDateAsc(assetId, TransactionAction.SELL))
                .thenReturn(List.of());

        // Asset has 100 units worth 500000 cost basis -> avg price = 5000
        AssetsPnLResponseDTO result = pnLCalculationService.computePnLForAsset(asset);

        // Should fall back to 500000 / 100 = 5000
        assertEquals(BigDecimal.valueOf(5000).setScale(4, RoundingMode.HALF_UP), result.getAvgPrice());
        
        // Potential PnL: 100 * (5500 - 5000) = 50000
        assertEquals(BigDecimal.valueOf(50000).setScale(2, RoundingMode.HALF_UP), result.getPotentialPnL());
    }
}