package com.softdreams.activityhub.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.softdreams.activityhub.dto.request.ApiResponse;
import com.softdreams.activityhub.dto.request.StockTransactionRequest;
import com.softdreams.activityhub.dto.response.StockTransactionResponse;
import com.softdreams.activityhub.enums.StockTransactionType;
import com.softdreams.activityhub.service.StockTransactionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/stock-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockTransactionController {
    StockTransactionService stockTransactionService;

    @PostMapping
    ApiResponse<StockTransactionResponse> create(@RequestBody @Valid StockTransactionRequest request) {
        return ApiResponse.<StockTransactionResponse>builder()
                .result(stockTransactionService.createManual(request))
                .build();
    }

    @GetMapping
    ApiResponse<Page<StockTransactionResponse>> search(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) StockTransactionType type,
            @RequestParam(required = false) String productId) {
        return ApiResponse.<Page<StockTransactionResponse>>builder()
                .result(stockTransactionService.search(type, productId, pageable))
                .build();
    }
}
