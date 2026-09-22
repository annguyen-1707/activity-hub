package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
    USER("Người dùng"),
    ORDER("Đơn hàng"),
    ROLE("Vai trò"),
    PRODUCT("Sản phẩm"),
    CATEGORY("Danh mục"),
    REVIEW("Đánh giá");
    private final String label;
}
