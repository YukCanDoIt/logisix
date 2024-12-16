package com.sparta.core.exception;

public record ExceptionResponse(
    String message
) {

  public String toWrite() {
    return "{" +
        "\"message\":" + "\"" + message + "\"" +
        "}";
  }
}