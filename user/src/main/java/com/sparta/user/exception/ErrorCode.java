package com.sparta.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 일반 에러
    API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "API 호출이 실패했습니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
    UNSUPPORTED_SORT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 방식입니다."),

    // 인증 및 권한 에러
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 존재하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호 정보가 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
    INVALID_SIGNATURE(HttpStatus.FORBIDDEN, "유효하지 않은 게이트웨이 서명입니다."),

    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "일치하는 유저 정보가 존재하지 않습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "유저 이름이 이미 존재합니다."),
    MISSING_USER_INFORMATION(HttpStatus.BAD_REQUEST, "헤더에 사용자 정보가 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID입니다."),

    // 요청 처리 에러
    MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, "필수 헤더가 누락되었습니다."),
    INVALID_REQUEST_PATH(HttpStatus.BAD_REQUEST, "유효하지 않은 요청 경로입니다."),
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 요청 파라미터입니다."),
    INVALID_REQUEST_DATA(HttpStatus.BAD_REQUEST, "잘못된 정보를 요청하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
