package com.softdreams.activityhub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.softdreams.activityhub.dto.request.UserCreationRequest;
import com.softdreams.activityhub.dto.request.UserUpdateRequest;
import com.softdreams.activityhub.dto.response.UserResponse;
import com.softdreams.activityhub.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
