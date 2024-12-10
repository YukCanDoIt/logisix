package com.sparta.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data) {

    // 성공 상태
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiStatus.SUCCESS.getCode(), ApiStatus.SUCCESS.getMessage(), data);
    }

    // 실패 상태
    public static <T> ApiResponse<T> fail(ApiStatus status) {
        return new ApiResponse<>(status.getCode(), status.getMessage(), null);
    }
}