package com.softdreams.activityhub.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
        name = "product_reviews",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_product_user",
                    columnNames = {"product_id", "user_id"})
        })
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    Integer rating;

    @Column(columnDefinition = "NVARCHAR(1000)")
    String comment;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
