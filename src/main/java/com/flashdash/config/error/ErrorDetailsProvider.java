package com.flashdash.config.error;

import com.flashdash.dto.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class ErrorDetailsProvider {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private final ResourceBundleMessageSource messageSource;

    public ErrorDetailsProvider() {
        this.messageSource = new ResourceBundleMessageSource();
        this.messageSource.setBasename("error-messages");
        this.messageSource.setDefaultEncoding("UTF-8");
    }

    public ErrorResponse createErrorResponse(ErrorCode errorCode) {
        return new ErrorResponse(
                getStatus(errorCode),
                errorCode.name(),
                getCause(errorCode),
                getAction(errorCode),
                LocalDateTime.now(),
                MDC.get(CORRELATION_ID_KEY)
        );
    }

    private int getStatus(ErrorCode errorCode) {
        return Integer.parseInt(getMessage(errorCode.name() + ".status"));
    }

    private String getCause(ErrorCode errorCode) {
        return getMessage(errorCode.name() + ".cause");
    }

    private String getAction(ErrorCode errorCode) {
        return getMessage(errorCode.name() + ".action");
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }
}
