package com.sparta.slack.controller;

import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.dto.SlackResponse;
import com.sparta.slack.service.SlackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/slack")
public class SlackController {

  private final SlackService slackService;

  public SlackController(SlackService slackService) {
    this.slackService = slackService;
  }

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
