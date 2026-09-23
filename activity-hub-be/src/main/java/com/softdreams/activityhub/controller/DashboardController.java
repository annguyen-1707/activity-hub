package com.softdreams.activityhub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.response.dashboard.DashboardOverviewResponse;
import com.softdreams.activityhub.service.DashboardService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {
    DashboardService dashboardService;

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> getOverview(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime toDate) {
        return ApiResponse.<DashboardOverviewResponse>builder()
                .result(dashboardService.getOverview(days, fromDate, toDate))
                .build();
    }
}

