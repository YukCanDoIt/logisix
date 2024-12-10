package com.sparta.common.response;

public enum ApiStatus {
    SUCCESS(200, "API 요청에 성공했습니다"),
    BAD_REQUEST(400, "잘못된 요청입니다"),
    SERVER_ERROR(500, "서버 에러가 발생했습니다");

    private final int code;
    private final String message;

    ApiStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}