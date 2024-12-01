package com.flashdash.exception;

import com.flashdash.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorDetailsProvider errorDetailsProvider;

    public GlobalExceptionHandler(ErrorDetailsProvider errorDetailsProvider) {
        this.errorDetailsProvider = errorDetailsProvider;
    }

    @ExceptionHandler(FlashDashException.class)
    public ResponseEntity<ErrorResponse> handleFlashDashException(FlashDashException ex) {
        String correlationId = UUID.randomUUID().toString();
        var errorCode = ex.getErrorCode();

        ErrorResponse errorResponse = new ErrorResponse(
                errorDetailsProvider.getStatus(errorCode),
                errorCode.name(),
                errorDetailsProvider.getCause(errorCode),
                errorDetailsProvider.getAction(errorCode),
                LocalDateTime.now(),
                correlationId
        );

        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }
}
