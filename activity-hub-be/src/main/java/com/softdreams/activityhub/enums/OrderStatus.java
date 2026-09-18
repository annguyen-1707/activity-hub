package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CREATED("Đã được tạo"),
    CONFIRMED("Đã được xác nhận"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã bị hủy");

    private final String label;
}
