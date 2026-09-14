package com.softdreams.activityhub.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        name = "activity_statistics",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_stat_date_event_type",
                    columnNames = {"stat_date", "event_type"})
        })
public class ActivityStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "unique_users", nullable = false)
    private Integer uniqueUsers;

    @Column(name = "success_count", nullable = false)
    private Integer successCount;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
