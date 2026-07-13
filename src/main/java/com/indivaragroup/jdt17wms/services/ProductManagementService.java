package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.request.ProductQueryDTO;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductManagementService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductManagementService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getProductsForUser(
            ProductQueryDTO queryDTO,
            Pageable pageable) {

        String searchQuery = queryDTO != null ? queryDTO.getSearchQuery() : null;
        String type = queryDTO != null ? queryDTO.getType() : null;
        Boolean showAll = queryDTO != null ? queryDTO.getShowAll() : null;
        Boolean dashboardSummary = queryDTO != null ? queryDTO.getDashboardSummary() : null;

        User user = userRepository.findById(AppConstants.USER_ID).orElse(null);

        // Check user risk profile questionnaire
        if (user != null &&
            user.getRole() == UserRole.USER &&
            (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted())
        ) {
                throw new MissingRiskProfileException("Risk Profiler Assessment Required");
            }


      List<Product> products = productRepository.findAll();

        // 1. Visibility filter
        if (user != null && user.getRole() == UserRole.USER) {
            products = products.stream()
                    .filter(p -> p.getVisible() != null && p.getVisible())
                    .toList();
        }

        // 2. Risk level filter
        if (user != null && user.getRole() == UserRole.USER) {
            boolean shouldShowAll = showAll != null && showAll;
            String riskProfile = user.getRiskProfile();
            boolean isRiskTaker = riskProfile != null && (riskProfile.equalsIgnoreCase("risk_taker"));

            if (!shouldShowAll && !isRiskTaker) {
                int maxRiskLevel = 5;
                if (riskProfile != null) {
                    if (riskProfile.equalsIgnoreCase("risk_averse")) {
                        maxRiskLevel = 2;
                    } else if (riskProfile.equalsIgnoreCase("moderate")) {
                        maxRiskLevel = 4;
                    }
                }
                final int limitRisk = maxRiskLevel;
                products = products.stream()
                        .filter(p -> p.getRiskLevel() != null && p.getRiskLevel() <= limitRisk)
                        .toList();
            }
        }

        // 3. Type filter
        if (type != null && !type.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getType() != null && p.getType().equalsIgnoreCase(type))
                    .toList();
        }

        // 4. Search query filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String query = searchQuery.toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(query))
                            || (p.getIssuer() != null && p.getIssuer().toLowerCase().contains(query)))
                    .toList();
        }

        // 5. Dashboard Summary limit
        if (dashboardSummary != null && dashboardSummary) {
            products = products.stream()
                    .limit(5)
                    .toList();
        }

        // 6. Pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());

        if (start > products.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, products.size());
        }

        List<Product> pageContent = products.subList(start, end);
        return new PageImpl<>(pageContent, pageable, products.size());
    }

    public Product updateProductVisibility(UUID id, Boolean visibility) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No valid item with the ID"));
        product.setVisible(visibility);
        return productRepository.save(product);
    }
}


