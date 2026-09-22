package com.softdreams.activityhub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.softdreams.activityhub.dto.response.ActivityLogResponse;
import com.softdreams.activityhub.entity.ActivityLog;
import com.softdreams.activityhub.entity.User;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(target = "eventTypeLabel", source = "eventType.label")
    @Mapping(target = "targetTypeLabel", source = "targetType.label")
    ActivityLogResponse toResponse(ActivityLog activityLog);

    default String fullName(User user) {
        if (user == null) {
            return null;
        }

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (lastName + " " + firstName).trim();

        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
}
