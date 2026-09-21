package com.softdreams.activityhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST_BODY(1001, "Invalid body request", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ORDER_NOT_EXISTED(1009, "Order not existed", HttpStatus.NOT_FOUND),
    INVALID_ENUM_VALUE(1010, "Invalid value enum for field", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(1011, "Role not existed", HttpStatus.NOT_FOUND),
    ROLE_IN_USE(1012, "Role is assigned to users and cannot be deleted", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTED(1013, "Product not existed", HttpStatus.NOT_FOUND),
    PRODUCT_IN_USE(1014, "Product has existing orders or stock history and cannot be deleted", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(1015, "Insufficient stock for product", HttpStatus.BAD_REQUEST),
    INVALID_STOCK_TRANSACTION_TYPE(1016, "This transaction type cannot be created manually", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTED(1017, "Category not existed", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(1018, "Category code or name already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_IN_USE(1019, "Category has associated products and cannot be deleted", HttpStatus.BAD_REQUEST),
    ORDER_LINE_NOT_EXISTED(1020, "Order line not existed", HttpStatus.NOT_FOUND),
    ORDER_NOT_COMPLETED(1021, "Order must be completed before it can be reviewed", HttpStatus.BAD_REQUEST),
    REVIEW_WINDOW_EXPIRED(1022, "Reviews are only allowed within 30 days of delivery", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(1023, "This order line has already been reviewed", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_EXISTED(1024, "Review not existed", HttpStatus.NOT_FOUND),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
