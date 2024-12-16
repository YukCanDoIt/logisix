package com.sparta.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    private static final String ERROR_LOG = "[ERROR] %s %s";

    @ExceptionHandler(LogisixException.class)
    public ResponseEntity<ExceptionResponse> handleApplicationException(final LogisixException e) {
        log.error(String.format(ERROR_LOG, e.getHttpStatus(), e.getMessage()));
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoResourceFoundException.class)
    public ExceptionResponse handleNoResourceFoundException(NoResourceFoundException e) {
        log.error(String.format(ERROR_LOG, e.getMessage(), e.getClass().getName()));
        return new ExceptionResponse("지원하지 않는 경로입니다.");
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ExceptionResponse handleHttpRequestMethodNotSupportedException(
            final HttpRequestMethodNotSupportedException e) {
        log.error(String.format(ERROR_LOG, e.getMessage(), Arrays.toString(e.getSupportedMethods())));
        return new ExceptionResponse("지원하지 않는 요청 방법입니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ExceptionResponse handleMissingServletRequestParameterException(
            final MissingServletRequestParameterException e) {
        log.error(String.format(ERROR_LOG, e.getParameterName(), e.getMessage()));
        return new ExceptionResponse("필요한 파라미터가 입력되지 않았습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionResponse handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException e) {
        log.error(String.format(ERROR_LOG, e.getMessage(), e.getClass().getName()));
        return new ExceptionResponse(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }
}
