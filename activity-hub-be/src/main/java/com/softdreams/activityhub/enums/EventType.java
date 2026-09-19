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
    CANCEL("CANCEL"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    DONE("DONE"),
    ;

    private final String label;
}
