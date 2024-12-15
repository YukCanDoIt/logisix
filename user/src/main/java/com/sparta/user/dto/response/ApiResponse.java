package com.sparta.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data) {

    private static final String SUCCESS = "Api 요청이 성공적으로 이루어졌습니다.";

    // 성공 상태
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, SUCCESS, data);
    }
}