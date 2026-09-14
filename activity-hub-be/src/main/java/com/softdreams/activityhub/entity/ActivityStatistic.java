package com.softdreams.activityhub.entity;

import java.time.LocalDate;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;
}
