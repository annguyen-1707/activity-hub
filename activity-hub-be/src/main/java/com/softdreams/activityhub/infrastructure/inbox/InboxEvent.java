package com.softdreams.activityhub.infrastructure.inbox;

import com.softdreams.activityhub.enums.InboxStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inbox_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inbox_event_id",
                        columnNames = "event_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class InboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String eventType;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private InboxStatus status;

    private LocalDateTime receivedAt;

    private LocalDateTime processedAt;

    private String errorMessage;
}
