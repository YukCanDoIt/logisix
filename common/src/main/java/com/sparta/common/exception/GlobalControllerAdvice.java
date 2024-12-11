package com.sparta.common.exception;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    private static final String ERROR_LOG = "[ERROR] Action: {}, Error: {}";

    @ExceptionHandler(LogisixException.class)
    public ResponseEntity<ExceptionResponse> logisixException(final LogisixException e) {
        log.error(ERROR_LOG, e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(new ExceptionResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ExceptionResponse handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error(ERROR_LOG, e.getRequestURL(), e.getHttpMethod());
        return new ExceptionResponse("요청한 리소스를 찾을 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ExceptionResponse httpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException e) {
        log.error(ERROR_LOG, e.getMethod(), Arrays.toString(e.getSupportedMethods()));
        return new ExceptionResponse("지원하지 않는 HTTP 메소드입니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ExceptionResponse missingServletRequestParameter(final MissingServletRequestParameterException e) {
        log.error(ERROR_LOG, e.getParameterName(), e.getMessage());
        return new ExceptionResponse("필수 요청 파라미터가 누락되었습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionResponse methodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error(ERROR_LOG, e.getBindingResult().getTarget(), e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ExceptionResponse(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}