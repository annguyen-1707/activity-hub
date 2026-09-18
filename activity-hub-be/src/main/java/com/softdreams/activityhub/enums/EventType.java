package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    CREATED("Đã được tạo"),
    UPDATED("Đã được cập nhật"),
    DELETED("Đã bị xóa"),
    APPROVED("Đã được phê duyệt"),
    REJECTED("Đã bị từ chối"),
    LOGIN("Đã đăng nhập"),
    LOGOUT("Đã đăng xuất"),
    ;

    private final String label;
}
