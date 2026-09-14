package com.softdreams.activityhub.exception;

public class AppException extends RuntimeException {

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public AppException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    private ErrorCode errorCode;
    private final String message;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
