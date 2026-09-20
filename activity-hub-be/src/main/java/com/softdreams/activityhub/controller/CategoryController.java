package com.softdreams.activityhub.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.request.CategoryRequest;
import com.softdreams.activityhub.dto.response.CategoryResponse;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.service.CategoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ActivityLog(eventType = EventType.CREATED, targetType = TargetType.CATEGORY, targetId = "#result.result.id")
    ApiResponse<CategoryResponse> create(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<CategoryResponse>> getAll() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllActive())
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Page<CategoryResponse>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ApiResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.search(keyword, pageable))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<CategoryResponse> getById(@PathVariable String id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ActivityLog(eventType = EventType.UPDATED, targetType = TargetType.CATEGORY, targetId = "#id")
    ApiResponse<CategoryResponse> update(@PathVariable String id, @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ActivityLog(eventType = EventType.DELETED, targetType = TargetType.CATEGORY, targetId = "#id")
    ApiResponse<Void> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
