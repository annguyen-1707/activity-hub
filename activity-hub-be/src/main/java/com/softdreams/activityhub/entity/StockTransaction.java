package com.softdreams.activityhub.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.BatchSize;

import com.softdreams.activityhub.enums.StockTransactionType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "stock_transactions",
        indexes = {
            @Index(name = "idx_stock_transactions_created_at", columnList = "created_at"),
            @Index(name = "idx_stock_transactions_type_created_at", columnList = "type, created_at"),
            @Index(name = "idx_stock_transactions_created_by", columnList = "created_by")
        })
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    StockTransactionType type;

    @Column(name = "reference_id")
    String referenceId;

    @Column(columnDefinition = "NVARCHAR(500)")
    String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    User createdBy;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "stockTransaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    List<StockTransactionLine> lines;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
