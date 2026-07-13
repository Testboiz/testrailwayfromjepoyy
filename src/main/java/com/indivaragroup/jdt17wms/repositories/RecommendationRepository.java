package com.indivaragroup.jdt17wms.repositories;

import com.indivaragroup.jdt17wms.models.Recommendation;
import com.indivaragroup.jdt17wms.models.enums.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findAllByResolvedByAssetId(UUID resolvedByAssetId);
    List<Recommendation> findAllByUserIdAndStatus(UUID userId, RecommendationStatus status);
}
