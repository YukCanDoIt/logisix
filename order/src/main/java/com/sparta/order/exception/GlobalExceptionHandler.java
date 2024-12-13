package com.sparta.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  // UnauthorizedException 처리
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_JSON) // Content-Type 명시
        .body("{\"message\": \"" + ex.getMessage() + "\"}");
  }

  // 기타 예외 처리 (필요시 추가)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON) // Content-Type 명시
        .body("{\"message\": \"서버 에러가 발생했습니다: " + ex.getMessage() + "\"}");
  }

}
