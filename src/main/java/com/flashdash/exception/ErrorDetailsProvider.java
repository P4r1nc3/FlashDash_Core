package com.flashdash.exception;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ErrorDetailsProvider {

    private final ResourceBundleMessageSource messageSource;

    public ErrorDetailsProvider() {
        this.messageSource = new ResourceBundleMessageSource();
        this.messageSource.setBasename("error-messages");
        this.messageSource.setDefaultEncoding("UTF-8");
    }

    public int getStatus(ErrorCode errorCode) {
        return Integer.parseInt(getMessage(errorCode.name() + ".status"));
    }

    public String getCause(ErrorCode errorCode) {
        return getMessage(errorCode.name() + ".cause");
    }

    public String getAction(ErrorCode errorCode) {
        return getMessage(errorCode.name() + ".action");
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }
}
