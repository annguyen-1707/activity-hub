package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethodEnum {

    BANK("Chuyển khoản"),
    CASH("Tiền mặt");

    private final String label;
}
