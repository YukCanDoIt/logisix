package com.sparta.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

  private final HttpStatus httpStatus;
  private final String message;

  public ApiException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.httpStatus = errorCode.getHttpStatus();
    this.message = errorCode.getMessage();
  }
}

