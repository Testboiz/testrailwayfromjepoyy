package com.indivaragroup.jdt17wms.models;

import com.indivaragroup.jdt17wms.models.enums.RecommendationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "priority")
    private String priority;

    @Column(name = "category")
    private String category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "suggested_amount", precision = 18, scale = 4)
    private BigDecimal suggestedAmount;

    @Column(name = "goal_id")
    private UUID goalId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommended_allocation", columnDefinition = "jsonb")
    private String recommendedAllocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "recommendation_status")
    private RecommendationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by_asset_id")
    private UUID resolvedByAssetId;
}
