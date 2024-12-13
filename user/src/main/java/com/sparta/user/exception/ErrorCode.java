package com.sparta.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "API 호출이 실패했습니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
    UNSUPPORTED_SORT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 방식입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 존재하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호 정보가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "일치하는 유저 정보가 존재하지 않습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "유저 이름이 이미 존재합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
