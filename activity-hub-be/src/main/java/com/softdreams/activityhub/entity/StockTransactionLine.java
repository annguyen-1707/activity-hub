package com.softdreams.activityhub.entity;

import jakarta.persistence.*;

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
        name = "stock_transaction_lines",
        indexes = {
            @Index(name = "idx_stock_tx_lines_transaction_id", columnList = "stock_transaction_id"),
            @Index(name = "idx_stock_tx_lines_product_id", columnList = "product_id, stock_transaction_id")
        })
public class StockTransactionLine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_transaction_id", nullable = false)
    StockTransaction stockTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    /**
     * Signed delta applied to {@code Product.quantity} when this line is posted
     * (e.g. +50 for an IMPORT, -2 for a SALE). The header's {@code type} is only
     * descriptive; this value is always the source of truth for the stock change.
     */
    @Column(nullable = false)
    Integer quantity;
}
