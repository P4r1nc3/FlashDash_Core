package com.flashdash.exception;

public class FlashDashException extends RuntimeException {

    private final ErrorCode errorCode;

    public FlashDashException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
