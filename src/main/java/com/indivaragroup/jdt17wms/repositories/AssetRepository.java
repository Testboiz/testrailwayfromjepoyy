package com.indivaragroup.jdt17wms.repositories;

import com.indivaragroup.jdt17wms.models.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    @Query("SELECT SUM(a.amount) FROM Asset a")
    BigDecimal sumTotalAmount();

    List<Asset> findAllByPurchaseDateGreaterThanEqual(Instant purchaseDate);
    List<Asset> findAllByUserId(UUID id);
    List<Asset> findAllByGoalId(UUID goalId);
//    List<Asset> findAllByPurchaseDateLessThanEqual(Instant purchaseDate);
}
