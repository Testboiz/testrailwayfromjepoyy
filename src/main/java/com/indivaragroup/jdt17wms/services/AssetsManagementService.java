package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.aspects.RiskProfileAssessmentRequired;
import com.indivaragroup.jdt17wms.dto.request.AssetRegistrationDTO;
import com.indivaragroup.jdt17wms.dto.request.GoalSettingDTO;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.Recommendation;
import com.indivaragroup.jdt17wms.models.TransactionHistory;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.TransactionAction;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.RecommendationRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AssetsManagementService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ProductRepository productRepository;
    private final GoalRepository goalRepository;
    private final RecommendationRepository recommendationRepository;

    public AssetsManagementService(AssetRepository assetRepository,
                                   UserRepository userRepository,
                                   TransactionHistoryRepository transactionHistoryRepository,
                                   ProductRepository productRepository,
                                   GoalRepository goalRepository,
                                   RecommendationRepository recommendationRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.productRepository = productRepository;
        this.goalRepository = goalRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @RiskProfileAssessmentRequired
    public List<Asset> getAssetsForUser() {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));
        return assetRepository.findAllByUserId(user.getId());
    }

    @RiskProfileAssessmentRequired
    public List<TransactionHistory> getTransactionLogsForUser() {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));
        return transactionHistoryRepository.findAllByUserId(user.getId());
    }

    @Transactional
    @RiskProfileAssessmentRequired
    public Asset createAssetForUser(AssetRegistrationDTO dto) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new CoreThrowHandler(ApiError.NOT_FOUND,"No valid item with the ID"));

        if (!Boolean.TRUE.equals(product.getVisible())) {
            throw new CoreThrowHandler(ApiError.DELISTED_PRODUCT);
        }



        Instant purchaseInstant = dto.getPurchaseDate().atZone(ZoneId.systemDefault()).toInstant();

        Asset asset = Asset.builder()
                .userId(user.getId())
                .productId(product.getId())
                .units(dto.getUnits())
                .amount(dto.getAmount())
                .currentValue(dto.getCurrentValue())
                .purchaseDate(purchaseInstant)
                .platform(dto.getPlatform())
                .notes(dto.getNotes())
                .build();

        Asset savedAsset = assetRepository.save(asset);

        // Record BUY transaction log
        BigDecimal pricePerUnit = dto.getAmount().divide(dto.getUnits(), 4, RoundingMode.HALF_UP);

        TransactionHistory buyHistory = TransactionHistory.builder()
                .userId(user.getId())
                .productId(product.getId())
                .assetId(savedAsset.getId())
                .action(TransactionAction.BUY)
                .pricePerUnit(pricePerUnit)
                .units(dto.getUnits())
                .totalAmount(dto.getAmount())
                .transactionDate(purchaseInstant)
                .notes(dto.getNotes())
                .build();

        transactionHistoryRepository.save(buyHistory);

        return savedAsset;
    }

    @Transactional
    @RiskProfileAssessmentRequired
    public Asset updateAssetForUser(UUID assetId, GoalSettingDTO dto) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new CoreThrowHandler(ApiError.ITEM_NOT_FOUND));

        if (!asset.getUserId().equals(user.getId())) {
            throw new CoreThrowHandler(ApiError.ITEM_NOT_FOUND);
        }

        if (dto.getGoalId() != null) {
            goalRepository.findById(dto.getGoalId())
                    .orElseThrow(() -> new CoreThrowHandler(ApiError.ITEM_NOT_FOUND));
            asset.setGoalId(dto.getGoalId());
        } else {
            asset.setGoalId(null);
        }

        return assetRepository.save(asset);
    }

    @Transactional
    @RiskProfileAssessmentRequired
    public void deleteAssetForUser(UUID assetId) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new CoreThrowHandler(ApiError.ITEM_NOT_FOUND));

        if (!asset.getUserId().equals(user.getId())) {
            throw new CoreThrowHandler(ApiError.ITEM_NOT_FOUND);
        }

        // Record SELL transaction log
        BigDecimal pricePerUnit = BigDecimal.ZERO;
        if (Objects.requireNonNullElse(asset.getUnits(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            pricePerUnit = asset.getAmount().divide(asset.getUnits(), 4, RoundingMode.HALF_UP);
        }

        TransactionHistory sellHistory = TransactionHistory.builder()
                .userId(user.getId())
                .productId(asset.getProductId())
                .assetId(asset.getId())
                .action(TransactionAction.SELL)
                .pricePerUnit(pricePerUnit)
                .units(asset.getUnits())
                .totalAmount(asset.getAmount())
                .transactionDate(Instant.now())
                .notes("Asset sold via deletion")
                .build();

        transactionHistoryRepository.save(sellHistory);

        // Deallocate resolution references in recommendations to prevent constraint violations
        List<Recommendation> recommendations = recommendationRepository.findAllByResolvedByAssetId(asset.getId());
        for (Recommendation recommendation : recommendations) {
            recommendation.setResolvedByAssetId(null);
            recommendationRepository.save(recommendation);
        }

        assetRepository.delete(asset);
    }
}
