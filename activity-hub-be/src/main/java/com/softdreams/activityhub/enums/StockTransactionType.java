package com.softdreams.activityhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockTransactionType {

    IMPORT("Nhập hàng"),
    SALE("Bán hàng"),
    CANCEL("Hủy đơn"),
    ADJUSTMENT("Điều chỉnh tồn kho");

    private final String label;
}
