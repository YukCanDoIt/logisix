package com.sparta.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    int statusCode,
    T data) {

  public static ApiResponse<Void> success() {
    return new ApiResponse<>(200, null);
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(200, data);
  }
}