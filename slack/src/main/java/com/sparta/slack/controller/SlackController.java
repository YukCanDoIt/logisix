package com.sparta.slack.controller;

import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.dto.SlackResponse;
import com.sparta.slack.service.SlackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/slack")
@Tag(name = "Slack API", description = "Slack 메시지 전송 API")
public class SlackController {

  private final SlackService slackService;

  public SlackController(SlackService slackService) {
    this.slackService = slackService;
  }

  @Operation(summary = "Slack 메시지 전송", description = "Slack 채널로 메시지를 전송합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
  })
  @PostMapping("/send")
  public ResponseEntity<SlackResponse> sendSlackMessage(@RequestBody @Valid SlackRequest request) {
    try {
      String response = slackService.sendMessage(request);
      return ResponseEntity.ok(new SlackResponse(true, response, request.channel()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new SlackResponse(false, e.getMessage(), request.channel()));
    }
  }
}
