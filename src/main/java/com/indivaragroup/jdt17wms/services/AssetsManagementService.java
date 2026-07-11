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
import com.indivaragroup.jdt17wms.models.enums.TransactionAction;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ExpenseRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
public class AssetsManagementService {

    private final AssetRepository assetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ProductRepository productRepository;

    public AssetsManagementService(AssetRepository assetRepository,
                                   ExpenseRepository expenseRepository,
                                   UserRepository userRepository,
                                   TransactionHistoryRepository transactionHistoryRepository,
                                   ProductRepository productRepository) {
        this.assetRepository = assetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.productRepository = productRepository;
    }

    public List<Asset> getAssetsForUser() {
        User user = userRepository.findById(AppConstants.USER_ID)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }
        return assetRepository.findAllByUserId(user.getId());
    }

    public List<TransactionHistory> getTransactionLogsForUser() {
        User user = userRepository.findById(AppConstants.USER_ID)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }
        return transactionHistoryRepository.findAllByUserId(user.getId());
    }

    @Transactional
    public Asset createAssetForUser(AssetRegistrationDTO dto) {
        User user = userRepository.findById(AppConstants.USER_ID)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("No valid item with the ID"));

        if (product.getVisible() == null || !product.getVisible()) {
            throw new DelistedProductException("Can’t track delisted products");
        }

        Instant purchaseInstant;
        if (dto.getPurchaseDate() != null) {
            purchaseInstant = dto.getPurchaseDate().atZone(ZoneId.systemDefault()).toInstant();
        } else {
            purchaseInstant = Instant.now();
        }

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

        BigDecimal pricePerUnit = BigDecimal.ZERO;
        if (dto.getUnits() != null && dto.getUnits().compareTo(BigDecimal.ZERO) > 0) {
            pricePerUnit = dto.getAmount().divide(dto.getUnits(), 4, RoundingMode.HALF_UP);
        }

        TransactionHistory history = TransactionHistory.builder()
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

        transactionHistoryRepository.save(history);

        return savedAsset;
    }
}
