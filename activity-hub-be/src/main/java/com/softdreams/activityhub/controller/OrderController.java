package com.softdreams.activityhub.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.request.OrderRequest;
import com.softdreams.activityhub.dto.response.OrderResponse;
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
            @RequestParam(required = false) LocalDateTime toDate
            ) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.searchMyOrders(keyword,status,paymentMethod,
                        fromDate,toDate,pageable))
                .build();
    }
}
