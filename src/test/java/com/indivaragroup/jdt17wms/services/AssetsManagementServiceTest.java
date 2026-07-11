package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.request.AssetRegistrationDTO;
import com.indivaragroup.jdt17wms.exceptions.DelistedProductException;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.TransactionHistory;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ExpenseRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetsManagementServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AssetsManagementService assetsManagementService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(assetsManagementService);
    }

    @Test
    void getAssetsForUser_shouldReturnAssetsWhenQuestionnaireCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();

        Asset asset = Asset.builder().id(UUID.randomUUID()).userId(AppConstants.USER_ID).build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(assetRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(asset));

        List<Asset> result = assetsManagementService.getAssetsForUser();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(asset, result.get(0));
    }

    @Test
    void getAssetsForUser_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireNotCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> {
            assetsManagementService.getAssetsForUser();
        });
    }

    @Test
    void getAssetsForUser_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            assetsManagementService.getAssetsForUser();
        });
    }

    @Test
    void getTransactionLogsForUser_shouldReturnLogsWhenQuestionnaireCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();

        TransactionHistory log = TransactionHistory.builder().id(UUID.randomUUID()).userId(AppConstants.USER_ID).build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(transactionHistoryRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(log));

        List<TransactionHistory> result = assetsManagementService.getTransactionLogsForUser();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(log, result.get(0));
    }

    @Test
    void getTransactionLogsForUser_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireNotCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> {
            assetsManagementService.getTransactionLogsForUser();
        });
    }

    @Test
    void getTransactionLogsForUser_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            assetsManagementService.getTransactionLogsForUser();
        });
    }

    @Test
    void createAssetForUser_shouldCreateAssetAndRecordTransactionHistory_whenValid() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .visible(true)
                .build();

        AssetRegistrationDTO dto = AssetRegistrationDTO.builder()
                .productId(product.getId())
                .units(new BigDecimal("10.0"))
                .amount(new BigDecimal("100.0"))
                .currentValue(new BigDecimal("110.0"))
                .purchaseDate(LocalDateTime.of(2024, 5, 20, 10, 0, 0))
                .platform("Bibit")
                .notes("New investment")
                .build();

        Asset savedAsset = Asset.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .productId(product.getId())
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(assetRepository.save(any(Asset.class))).thenReturn(savedAsset);

        Asset result = assetsManagementService.createAssetForUser(dto);

        assertNotNull(result);
        assertEquals(savedAsset, result);
        verify(assetRepository).save(any(Asset.class));
        verify(transactionHistoryRepository).save(any(TransactionHistory.class));
    }

    @Test
    void createAssetForUser_shouldThrowDelistedProductException_whenProductIsHidden() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .visible(false)
                .build();

        AssetRegistrationDTO dto = AssetRegistrationDTO.builder()
                .productId(product.getId())
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(DelistedProductException.class, () -> {
            assetsManagementService.createAssetForUser(dto);
        });
    }

    @Test
    void createAssetForUser_shouldThrowMissingRiskProfileException_whenQuestionnaireNotCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();

        AssetRegistrationDTO dto = AssetRegistrationDTO.builder()
                .productId(UUID.randomUUID())
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> {
            assetsManagementService.createAssetForUser(dto);
        });
    }

    @Test
    void createAssetForUser_shouldThrowNotFoundException_whenProductNotFound() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();

        UUID productId = UUID.randomUUID();
        AssetRegistrationDTO dto = AssetRegistrationDTO.builder()
                .productId(productId)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            assetsManagementService.createAssetForUser(dto);
        });
    }
}
