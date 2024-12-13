package com.sparta.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LogisixException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;

    public LogisixException(ErrorCode errorCode) {
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
