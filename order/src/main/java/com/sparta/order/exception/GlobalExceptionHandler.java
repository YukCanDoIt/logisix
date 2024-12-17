package com.sparta.order.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(LogisixException.class)
  public ResponseEntity<ExceptionResponse> handleApplicationException(final LogisixException e, WebRequest request) {
    log.error("[ERROR] {} - {}", e.getHttpStatus(), e.getMessage());
    return ResponseEntity.status(e.getHttpStatus())
        .body(ExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(e.getHttpStatus().value())
            .error(e.getHttpStatus().getReasonPhrase())
            .message(e.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException e, WebRequest request) {
    log.error("[ERROR] {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error(HttpStatus.FORBIDDEN.getReasonPhrase())
            .message(e.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException e, WebRequest request) {
    log.error("[ERROR] No resource found - {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("지원하지 않는 경로입니다.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e, WebRequest request) {
    log.error("[ERROR] Method not supported - {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.METHOD_NOT_ALLOWED.value())
            .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
            .message("지원하지 않는 요청 방법입니다.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e, WebRequest request) {
    log.error("[ERROR] Missing parameter - {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("필요한 파라미터가 입력되지 않았습니다.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e, WebRequest request) {
    log.error("[ERROR] Validation failed - {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionResponse> handleGlobalException(Exception e, WebRequest request) {
    log.error("[ERROR] Internal server error - {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("서버 에러가 발생했습니다: " + e.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build());
  }
}
