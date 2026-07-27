package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.request.AdminProductCreateDTO;

import com.indivaragroup.jdt17wms.dto.response.ProductResponseDTO;
import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AdminProductManagementService {

    private final ProductRepository productRepository;

    public AdminProductManagementService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponseDTO> listProducts(String search, String type, Pageable pageable) {
        String s = (search != null && search.trim().isEmpty()) ? null : search;
        String t = (type != null && type.trim().isEmpty()) ? null : type;
        return productRepository.findAllAdmin(s, t, pageable).map(ProductResponseDTO::fromEntity);
    }

    public ProductResponseDTO createProduct(AdminProductCreateDTO dto) {
        Product p = Product.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .issuer(dto.getIssuer())
                .type(dto.getType())
                .riskLevel(dto.getRiskLevel())
                .annualReturn(dto.getAnnualReturn())
                .minInvestment(dto.getMinInvestment())
                .currentPrice(dto.getCurrentPrice())
                .description(dto.getDescription())
                .tenor(dto.getTenor())
                .lotSize(dto.getLotSize())
                .isFractionalAllowed(dto.getIsFractionalAllowed())
                .visible(dto.getVisible())
                .build();
        return ProductResponseDTO.fromEntity(productRepository.save(p));
    }
}
