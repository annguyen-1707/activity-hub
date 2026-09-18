package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryEnum {
    PHONE("Điện thoại"),
    LAPTOP("Laptop"),
    AUDIO("Âm thanh"),
    ACCESSORY("Phụ kiện"),
    MONITOR("Màn hình"),
    WATCH("Đồng hồ"),
    ;

    private final String label;
}
