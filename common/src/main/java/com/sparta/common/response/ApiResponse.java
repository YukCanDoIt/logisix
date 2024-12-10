package com.sparta.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiStatus.SUCCESS.getCode(), ApiStatus.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ApiStatus status) {
        return new ApiResponse<>(status.getCode(), status.getMessage(), null);
    }
}