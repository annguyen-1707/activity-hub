package com.softdreams.activityhub.mapper;

import org.mapstruct.Mapper;

import com.softdreams.activityhub.dto.request.RoleRequest;
import com.softdreams.activityhub.dto.response.RoleResponse;
import com.softdreams.activityhub.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
