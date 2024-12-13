package com.sparta.slack.dto;

public record SlackResponse(
    boolean ok,  // 메시지가 정상적으로 전송되었는지 여부
    String message,  // 응답 메시지
    String channel  // 메시지가 전송된 채널
) {}
