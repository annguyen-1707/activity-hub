package com.softdreams.activityhub.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.request.ProductRequest;
import com.softdreams.activityhub.dto.response.ProductResponse;
import com.softdreams.activityhub.enums.CategoryEnum;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.service.ProductService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @PostMapping
    @ActivityLog(eventType = EventType.CREATED, targetType = TargetType.PRODUCT, targetId = "#result.result.id")
    ApiResponse<ProductResponse> create(@RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<Page<ProductResponse>> search(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String category) {
        String cat = categoryId != null && !categoryId.isBlank() ? categoryId : category;
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.search(keyword, cat, pageable))
                .build();
    }

    @GetMapping("/{productId}")
    ApiResponse<ProductResponse> getById(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getById(productId))
                .build();
    }

    @PatchMapping("/{productId}")
    ApiResponse<ProductResponse> update(@PathVariable String productId, @RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.update(productId, request))
                .build();
    }

    @DeleteMapping("/{productId}")
    ApiResponse<Void> delete(@PathVariable String productId) {
        productService.delete(productId);
        return ApiResponse.<Void>builder().build();
    }
}
