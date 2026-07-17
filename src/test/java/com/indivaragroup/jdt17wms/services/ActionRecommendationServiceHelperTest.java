package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.Recommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ActionRecommendationServiceHelperTest {

    private ActionRecommendationService service;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);
        // Repositories are not needed for helper method tests; pass null.
        service = new ActionRecommendationService(null, null, null, null, null, null, null, fixedClock);
    }

    @Test
    void testRiskLabelVariousInputs() throws Exception {
        var method = ActionRecommendationService.class.getDeclaredMethod("riskLabel", String.class);
        method.setAccessible(true);
        // null input
        assertEquals("moderate", method.invoke(null, (Object) null));
        // explicit risk profiles
        assertEquals("risk-averse", method.invoke(null, "risk_averse"));
        assertEquals("risk-taker", method.invoke(null, "risk_taker"));
        // fallback to lower‑casing for unknown values
        assertEquals("custom", method.invoke(null, "custom"));
    }

    @Test
    void testFmtFormatting() throws Exception {
        var method = ActionRecommendationService.class.getDeclaredMethod("fmt", BigDecimal.class);
        method.setAccessible(true);
        // null value → "0"
        assertEquals("0", method.invoke(null, (Object) null));
        // normal formatting with grouping
        assertEquals("1,234", method.invoke(null, new BigDecimal("1234.56")));
        assertEquals("10,000", method.invoke(null, new BigDecimal("10000")));
    }

    @Test
    void testBestOfSelectionAndExclusion() throws Exception {
        var method = ActionRecommendationService.class.getDeclaredMethod("bestOf", List.class, List.class, int.class, Set.class);
        method.setAccessible(true);
        // Prepare products
        Product p1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Prod1")
                .type("stock")
                .riskLevel(3)
                .visible(true)
                .annualReturn(new BigDecimal("5"))
                .build();
        Product p2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Prod2")
                .type("bond")
                .riskLevel(2)
                .visible(true)
                .annualReturn(new BigDecimal("7"))
                .build();
        Product p3 = Product.builder()
                .id(UUID.randomUUID())
                .name("Prod3")
                .type("stock")
                .riskLevel(5)
                .visible(true)
                .annualReturn(new BigDecimal("10"))
                .build();
        Product p4 = Product.builder()
                .id(UUID.randomUUID())
                .name("Prod4")
                .type("stock")
                .riskLevel(2)
                .visible(false) // delisted/disabled
                .annualReturn(new BigDecimal("12"))
                .build();
        Product p5 = Product.builder()
                .id(UUID.randomUUID())
                .name("Prod5")
                .type("stock")
                .riskLevel(2)
                .visible(null) // delisted/disabled (null check)
                .annualReturn(new BigDecimal("15"))
                .build();
        List<Product> products = List.of(p1, p2, p3, p4, p5);
        // Choose type "stock", maxRisk 4, no exclusions → should pick p1 (risk within limit, highest return among allowed visible products)
        Product result = (Product) method.invoke(service, products, List.of("stock"), 4, null);
        assertNotNull(result);
        assertEquals(p1.getId(), result.getId());
        // Exclude p1 → no eligible stock product because p3 exceeds maxRisk 4, and p4/p5 are delisted
        result = (Product) method.invoke(service, products, List.of("stock"), 4, Set.of(p1.getId()));
        assertNull(result);
        // Type "bond" with maxRisk 3 → should pick p2
        result = (Product) method.invoke(service, products, List.of("bond"), 3, null);
        assertNotNull(result);
        assertEquals(p2.getId(), result.getId());
        // No matching visible product → null
        result = (Product) method.invoke(service, products, List.of("cash"), 5, null);
        assertNull(result);
    }

    @Test
    void testCalcLiquidValueAndOwnedTypes() throws Exception {
        var liquidMethod = ActionRecommendationService.class.getDeclaredMethod("calcLiquidValue", List.class, Map.class);
        var ownedMethod = ActionRecommendationService.class.getDeclaredMethod("calcOwnedTypes", List.class, Map.class);
        liquidMethod.setAccessible(true);
        ownedMethod.setAccessible(true);
        // Prepare products with types
        Product deposit = Product.builder()
                .id(UUID.randomUUID())
                .type("deposit")
                .build();
        Product stock = Product.builder()
                .id(UUID.randomUUID())
                .type("stock")
                .build();
        Map<UUID, Product> productMap = Map.of(deposit.getId(), deposit, stock.getId(), stock);
        // Assets: one liquid, one non‑liquid, one null value
        Asset a1 = Asset.builder().productId(deposit.getId()).currentValue(new BigDecimal("1000")).build();
        Asset a2 = Asset.builder().productId(stock.getId()).currentValue(new BigDecimal("2000")).build();
        Asset a3 = Asset.builder().productId(deposit.getId()).currentValue(null).build();
        List<Asset> assets = List.of(a1, a2, a3);
        BigDecimal liquid = (BigDecimal) liquidMethod.invoke(service, assets, productMap);
        assertEquals(new BigDecimal("1000"), liquid);
        @SuppressWarnings("unchecked")
        Set<String> owned = (Set<String>) ownedMethod.invoke(service, assets, productMap);
        assertTrue(owned.contains("deposit"));
        assertTrue(owned.contains("stock"));
        assertEquals(2, owned.size());
    }

    @Test
    void testRuleKeyGeneration() throws Exception {
        var method = ActionRecommendationService.class.getDeclaredMethod("ruleKey", Recommendation.class);
        method.setAccessible(true);
        // Build a recommendation with product and goal IDs
        UUID prodId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        Recommendation rec = Recommendation.builder()
                .category("test")
                .productId(prodId)
                .goalId(goalId)
                .build();
        String key = (String) method.invoke(null, rec);
        assertEquals("test:" + prodId + ":" + goalId, key);
        // Without product and goal
        Recommendation rec2 = Recommendation.builder()
                .category("other")
                .build();
        String key2 = (String) method.invoke(null, rec2);
        assertEquals("other:none:none", key2);
    }
}
