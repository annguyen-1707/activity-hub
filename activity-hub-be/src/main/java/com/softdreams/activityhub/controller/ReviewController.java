package com.softdreams.activityhub.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.request.ReviewRequest;
import com.softdreams.activityhub.dto.response.ReviewResponse;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.service.ReviewService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/order-lines/{orderLineId}/review")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {
    ReviewService reviewService;

    @PostMapping
    @ActivityLog(eventType = EventType.CREATED, targetType = TargetType.REVIEW, targetId = "#orderLineId")
    ApiResponse<ReviewResponse> create(@PathVariable String orderLineId, @RequestBody @Valid ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.create(orderLineId, request))
                .build();
    }

    @PutMapping
    @ActivityLog(eventType = EventType.UPDATED, targetType = TargetType.REVIEW, targetId = "#orderLineId")
    ApiResponse<ReviewResponse> update(@PathVariable String orderLineId, @RequestBody @Valid ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.update(orderLineId, request))
                .build();
    }

    @GetMapping
    ApiResponse<ReviewResponse> getByOrderLine(@PathVariable String orderLineId) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.getByOrderLine(orderLineId))
                .build();
    }
}
