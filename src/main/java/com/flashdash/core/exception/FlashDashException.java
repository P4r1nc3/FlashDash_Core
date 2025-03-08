package com.flashdash.core.exception;

public class FlashDashException extends RuntimeException {

    private final ErrorCode errorCode;

    public FlashDashException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
