package com.softdreams.activityhub.dto.report;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityLogReportItem {
    Integer stt;
    String username;
    String fullName;
    String eventTypeLabel;
    String targetTypeLabel;
    String targetId;
    String ipAddress;
    String createdAt;
}

