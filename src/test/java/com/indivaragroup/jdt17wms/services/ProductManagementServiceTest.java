package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.dto.request.ProductQueryDTO;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductManagementServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductManagementService productManagementService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(productManagementService);
    }

    @Test
    void getAllProducts_shouldReturnPaginatedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        Page<Product> expectedPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> actualPage = productManagementService.getAllProducts(pageable);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(product, actualPage.getContent().getFirst());
    }

    @Test
    void updateProductVisibility_shouldUpdateAndReturnProduct_whenProductExists() {
        UUID id = UUID.randomUUID();
        Product product = new Product();
        product.setId(id);
        product.setVisible(false);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updatedProduct = productManagementService.updateProductVisibility(id, true);

        assertNotNull(updatedProduct);
        assertTrue(updatedProduct.getVisible());
    }

    @Test
    void updateProductVisibility_shouldThrowNotFoundException_whenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CoreThrowHandler.class, () -> {
            productManagementService.updateProductVisibility(id, true);
        });
    }

    @Test
    void getProductsForUser_shouldThrowMissingRiskProfileException_whenUserQuestionnaireNotCompleted() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));

        ProductQueryDTO query = new ProductQueryDTO();
        Pageable pageRequest = PageRequest.of(0, 10);

        assertThrows(CoreThrowHandler.class, () -> {
          productManagementService.getProductsForUser(query, pageRequest);
        });
    }

    @Test
    void getProductsForUser_shouldFilterByVisibilityAndRiskProfile_forStandardUser() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("moderate") // moderate allows risk level <= 4
                .build();

        Product lowRiskVisible = Product.builder().riskLevel(2).visible(true).build();
        Product highRiskVisible = Product.builder().riskLevel(5).visible(true).build(); // Excluded (5 > 4)
        Product lowRiskHidden = Product.builder().riskLevel(2).visible(false).build(); // Excluded (hidden)

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(lowRiskVisible, highRiskVisible, lowRiskHidden));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(lowRiskVisible, result.getContent().getFirst());
    }

    @Test
    void getProductsForUser_shouldRespectShowAll_forStandardUser() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("moderate") // moderate allows risk level <= 4
                .build();

        Product lowRiskVisible = Product.builder().riskLevel(2).visible(true).build();
        Product highRiskVisible = Product.builder().riskLevel(5).visible(true).build(); // Included due to showAll = true

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(lowRiskVisible, highRiskVisible));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(null, null, true, false), PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getProductsForUser_shouldFilterByTypeAndSearchQuery() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();

        Product matchTypeAndName = Product.builder().name("Danareksa Stock").issuer("Danareksa").type("stock").visible(true).build();
        Product matchTypeOnly = Product.builder().name("BCA Stock").issuer("Bank BCA").type("stock").visible(true).build();
        Product matchNameOnly = Product.builder().name("Danareksa Bond").issuer("Danareksa").type("bond").visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(matchTypeAndName, matchTypeOnly, matchNameOnly));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO("Danareksa", "stock", false, false), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(matchTypeAndName, result.getContent().getFirst());
    }

    @Test
    void getProductsForUser_shouldLimitToDashboardSummary() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();

        List<Product> tenProducts = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> Product.builder().visible(true).build())
                .toList();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(tenProducts);

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(null, null, false, true), PageRequest.of(0, 10));

        assertEquals(5, result.getTotalElements());
    }

    @Test
    void getProductsForUser_shouldNotFilterByVisibilityAndRiskLevel_forAdmin() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.ADMIN)
                .build();

        Product visibleLowRisk = Product.builder().riskLevel(2).visible(true).build();
        Product hiddenHighRisk = Product.builder().riskLevel(5).visible(false).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(visibleLowRisk, hiddenHighRisk));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(), PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getProductsForUser_shouldNotFilter_whenUserDoesNotExist() {
        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.empty());
        Product visibleLowRisk = Product.builder().riskLevel(2).visible(true).build();
        Product hiddenHighRisk = Product.builder().riskLevel(5).visible(false).build();
        when(productRepository.findAll()).thenReturn(List.of(visibleLowRisk, hiddenHighRisk));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(), PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }



    @Test
    void getProductsForUser_shouldWork_whenQueryDtoIsNull() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();
        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        Product visibleLowRisk = Product.builder().riskLevel(2).visible(true).build();
        when(productRepository.findAll()).thenReturn(List.of(visibleLowRisk));

        Page<Product> result = productManagementService.getProductsForUser(null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductsForUser_shouldFilterByRiskAverse_forStandardUser() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_averse")
                .build();
        Product risk2 = Product.builder().riskLevel(2).visible(true).build();
        Product risk3 = Product.builder().riskLevel(3).visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(risk2, risk3));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(risk2, result.getContent().getFirst());
    }

    @Test
    void getProductsForUser_shouldDefaultMaxRiskLevelTo5_whenRiskProfileIsNull() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile(null)
                .build();
        Product risk5 = Product.builder().riskLevel(5).visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(risk5));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductsForUser_shouldExcludeProduct_whenRiskLevelIsNull() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("moderate")
                .build();
        Product riskNull = Product.builder().riskLevel(null).visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(riskNull));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(), PageRequest.of(0, 10));

        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getProductsForUser_shouldExcludeProduct_whenTypeFilterProvidedButProductTypeIsNull() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();
        Product typeNull = Product.builder().type(null).visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(typeNull));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(null, "stock", false, false), PageRequest.of(0, 10));

        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getProductsForUser_shouldMatchSearchQuery_whenEitherNameOrIssuerIsNull() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();
        Product nullNameMatchIssuer = Product.builder().name(null).issuer("Danareksa").visible(true).build();
        Product nullIssuerMatchName = Product.builder().name("Danareksa Stock").issuer(null).visible(true).build();
        Product bothNull = Product.builder().name(null).issuer(null).visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(nullNameMatchIssuer, nullIssuerMatchName, bothNull));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO("Danareksa", null, false, false), PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getProductsForUser_shouldReturnEmptyPage_whenOffsetIsGreaterThanProductListSize() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();
        Product product = Product.builder().visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(product));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(), PageRequest.of(1, 10));

        assertTrue(result.getContent().isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductsForUser_shouldNotFilterType_whenTypeFilterIsBlank() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();
        Product product = Product.builder().type("stock").visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(product));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO(null, "   ", false, false), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(product, result.getContent().getFirst());
    }

    @Test
    void getProductsForUser_shouldNotFilterSearchQuery_whenSearchQueryIsBlank() {
        User user = User.builder()
                .id(SecurityUtils.STATIC_USER_ID)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .riskProfile("risk_taker")
                .build();
        Product product = Product.builder().name("Danareksa Stock").visible(true).build();

        when(userRepository.findById(SecurityUtils.STATIC_USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(List.of(product));

        Page<Product> result = productManagementService.getProductsForUser(new ProductQueryDTO("   ", null, false, false), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(product, result.getContent().getFirst());
    }
}
