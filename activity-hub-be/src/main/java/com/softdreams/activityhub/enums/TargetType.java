package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
    USER("USER"),
    ORDER("ORDER"),
    ROLE("ROLE"),
    PRODUCT("PRODUCT"),
    PRODUCT_REVIEW("PRODUCT_REVIEW"),
    CATEGORY("CATEGORY");
    private final String label;
}
