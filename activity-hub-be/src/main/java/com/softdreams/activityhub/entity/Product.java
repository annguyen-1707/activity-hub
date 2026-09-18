package com.softdreams.activityhub.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.softdreams.activityhub.enums.CategoryEnum;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    String name;

    @Column(nullable = false, precision = 18, scale = 2)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    CategoryEnum category;

    double rate;

    @Column(nullable = false)
    int quantity;

    @Column(columnDefinition = "NVARCHAR(1000)")
    String description;

    String image;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
