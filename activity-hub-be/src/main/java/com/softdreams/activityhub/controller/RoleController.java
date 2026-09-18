package com.softdreams.activityhub.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.request.RoleRequest;
import com.softdreams.activityhub.dto.response.RoleResponse;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.service.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping
    @ActivityLog(eventType = EventType.CREATED, targetType = TargetType.ROLE, targetId = "#result.result.name")
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{role}")
    @ActivityLog(eventType = EventType.DELETED, targetType = TargetType.ROLE, targetId = "#role")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return ApiResponse.<Void>builder().build();
    }
}
