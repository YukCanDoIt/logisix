package com.sparta.order.exception;

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

  // 주문 관련 에러
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태입니다."),
  INVALID_ORDER_DATA(HttpStatus.BAD_REQUEST, "잘못된 주문 정보입니다."),

  // 여기서 HttpStatus와 메시지를 지정
  INVALID_REQUEST_DATA(HttpStatus.BAD_REQUEST, "유효하지 않은 요청 데이터입니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
