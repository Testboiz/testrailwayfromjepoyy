package com.indivaragroup.jdt17wms.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mst_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "risk_level", nullable = false)
    private Integer riskLevel;

    @Column(name = "annual_return", precision = 18, scale = 4, nullable = false)
    private BigDecimal annualReturn;

    @Column(name = "min_investment", precision = 18, scale = 4, nullable = false)
    private BigDecimal minInvestment;

    @Column(name = "current_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal currentPrice;

    @Column(name = "visible", nullable = false)
    private Boolean visible;

    @Column(name = "description", columnDefinition = "text", nullable = false)
    private String description;

    @Column(name = "tenor")
    private String tenor;

    @Column(name = "lot_size", nullable = false)
    private Integer lotSize;

    @Column(name = "is_fractional_allowed", nullable = false)
    private Boolean isFractionalAllowed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
