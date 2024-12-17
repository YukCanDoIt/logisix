package com.sparta.slack.dto;

public record SlackResponse(
    boolean ok,       // 메시지 전송 성공 여부
    String message,   // Slack API 응답 메시지
    String channel    // 메시지가 전송된 채널
) {
  // 정적 팩토리 메서드: SlackRequest, 응답 성공 여부, Slack API 응답 문자열을 받아 DTO 생성
  public static SlackResponse from(SlackRequest request, boolean ok, String slackApiResponse) {
    return new SlackResponse(
        ok,
        slackApiResponse,    // Slack API 호출의 응답 문자열
        request.channel()     // 요청에 있던 채널명
    );
  }
}
