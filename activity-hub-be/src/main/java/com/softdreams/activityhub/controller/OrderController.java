package com.softdreams.activityhub.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.request.OrderRequest;
import com.softdreams.activityhub.dto.response.OrderResponse;
import com.softdreams.activityhub.dto.response.OrderStatisticsResponse;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.service.OrderService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    OrderService orderService;

    @PostMapping
    @ActivityLog(eventType = EventType.CREATED, targetType = TargetType.ORDER, targetId = "#result.result.id")
    ApiResponse<OrderResponse> create(@RequestBody @Valid OrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<OrderResponse>> getAll() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAll())
                .build();
    }

    @GetMapping("/{orderId}")
    ApiResponse<OrderResponse> getById(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getById(orderId))
                .build();
    }

    @PutMapping("/{orderId}")
    @ActivityLog(eventType = EventType.UPDATED, targetType = TargetType.ORDER, targetId = "#orderId")
    ApiResponse<OrderResponse> update(@PathVariable String orderId, @RequestBody OrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.update(orderId, request))
                .build();
    }

    @DeleteMapping("/{orderId}")
    @ActivityLog(eventType = EventType.DELETED, targetType = TargetType.ORDER, targetId = "#orderId")
    ApiResponse<Void> delete(@PathVariable String orderId) {
        orderService.delete(orderId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/me")
    ApiResponse<Page<OrderResponse>> searchMyOrders(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.searchMyOrders(keyword, status, paymentMethod, fromDate, toDate, pageable))
                .build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Page<OrderResponse>> searchAdminOrders(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.searchAdminOrders(keyword, status, paymentMethod, fromDate, toDate, pageable))
                .build();
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<OrderStatisticsResponse> getAdminStatistics() {
        return ApiResponse.<OrderStatisticsResponse>builder()
                .result(orderService.getAdminStatistics())
                .build();
    }

    @PatchMapping("/{orderId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @ActivityLog(eventType = EventType.APPROVED, targetType = TargetType.ORDER, targetId = "#orderId")
    ApiResponse<OrderResponse> approve(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.approve(orderId))
                .build();
    }

    @PatchMapping("/{orderId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @ActivityLog(eventType = EventType.REJECTED, targetType = TargetType.ORDER, targetId = "#orderId")
    ApiResponse<OrderResponse> reject(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.reject(orderId))
                .build();
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)")
    @ActivityLog(eventType = EventType.CANCEL, targetType = TargetType.ORDER, targetId = "#orderId")
    ApiResponse<OrderResponse> cancel(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.cancel(orderId))
                .build();
    }

    @PatchMapping("/{orderId}/done")
    @PreAuthorize("hasRole('ADMIN')")
    @ActivityLog(eventType = EventType.DONE, targetType = TargetType.ORDER, targetId = "#orderId")
    ApiResponse<OrderResponse> done(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.done(orderId))
                .build();
    }

    //    @PatchMapping("/{orderId}/status")
    //    @PreAuthorize("hasRole('ADMIN')")
    //    ApiResponse<OrderResponse> updateStatus(@PathVariable String orderId, @RequestParam OrderStatus status) {
    //        return ApiResponse.<OrderResponse>builder()
    //                .result(orderService.updateStatus(orderId, status))
    //                .build();
    //    }
}
