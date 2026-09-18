package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    CREATED("CREATED"),
    UPDATED("UPDATED"),
    DELETED("DELETED"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    ;

    private final String label;
}
