package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ExpenseRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetsManagementServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

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

        Asset asset = Asset.builder().id(java.util.UUID.randomUUID()).userId(AppConstants.USER_ID).build();

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
}
