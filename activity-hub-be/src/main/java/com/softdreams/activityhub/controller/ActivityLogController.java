package com.softdreams.activityhub.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.response.ActivityLogResponse;
import com.softdreams.activityhub.dto.response.TargetTypeResponse;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.service.ActivityLogService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/activity-logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityLogController {
    ActivityLogService activityLogService;

    @GetMapping
    ApiResponse<Page<ActivityLogResponse>> search(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) TargetType targetType) {
        return ApiResponse.<Page<ActivityLogResponse>>builder()
                .result(activityLogService.search(pageable, keyword, eventType, targetType))
                .build();
    }

    @GetMapping({"/targets", "/target-types"})
    ApiResponse<List<TargetTypeResponse>> getTargetTypes() {
        return ApiResponse.<List<TargetTypeResponse>>builder()
                .result(activityLogService.getTargetTypes())
                .build();
    }
}
