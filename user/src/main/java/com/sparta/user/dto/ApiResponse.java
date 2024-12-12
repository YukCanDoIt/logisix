package com.sparta.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data) {

    private static final String SUCCESS = "Api 요청이 성공적으로 이루어졌습니다.";
    private static final String BAD_REQUEST = "RApi 요청이 실패하였습니다.(Bad_Request)";
    private static final String INTERNAL_SERVER_ERROR = "Api 요청이 실패하였습니다.(Internal_Server_Error)";

    // 성공 상태
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, SUCCESS, data);
    }

    // 클라이언트 오류 상태
    public static <T> ApiResponse<T> badRequest() {
        return new ApiResponse<>(400, BAD_REQUEST, null);
    }

    // 서버 오류 상태
    public static <T> ApiResponse<T> internalServerError() {
        return new ApiResponse<>(500, INTERNAL_SERVER_ERROR, null);
    }
}