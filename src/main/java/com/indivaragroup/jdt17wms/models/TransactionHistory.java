package com.indivaragroup.jdt17wms.models;

import com.indivaragroup.jdt17wms.models.enums.TransactionAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_transaction_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, columnDefinition = "transaction_action")
    private TransactionAction action;

    @Column(name = "price_per_unit", nullable = false, precision = 18, scale = 4)
    private BigDecimal pricePerUnit;

    @Column(name = "units", nullable = false, precision = 18, scale = 6)
    private BigDecimal units;

    @Column(name = "executed_lots", precision = 18, scale = 4)
    private BigDecimal executedLots;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalAmount;
}
