package com.softdreams.activityhub.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;

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
        name = "activity_logs",
        uniqueConstraints = {@UniqueConstraint(name = "uk_activity_event_id", columnNames = "event_id")},
        indexes = {
            @Index(name = "idx_activity_logs_created_at", columnList = "created_at"),
            @Index(name = "idx_activity_logs_user_id", columnList = "user_id"),
            @Index(name = "idx_activity_logs_event_type_created_at", columnList = "event_type, created_at")
        })
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 50)
    private TargetType targetType;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ip_address")
    String ipAddress;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
