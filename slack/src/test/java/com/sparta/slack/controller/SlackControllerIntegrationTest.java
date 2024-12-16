package com.sparta.slack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.slack.dto.OrderItemRequest;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.service.SlackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlackController.class)
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)
class SlackControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private SlackService slackService;

  @Test
  void sendSlackMessage_Success() throws Exception {
    // Given: Mock SlackService
    when(slackService.sendMessage(any(SlackRequest.class)))
        .thenReturn("Order is valid, message sent to Slack!");

    // 실제 SlackRequest 생성
    SlackRequest slackRequest = new SlackRequest(
        "YukCanDoIt",  // 채널 이름
        "Integration Test Message",  // 메시지
        UUID.randomUUID().toString(),  // 공급자 ID
        UUID.randomUUID().toString(),  // 수신자 ID
        UUID.randomUUID().toString(),  // 허브 ID
        List.of(new OrderItemRequest(UUID.randomUUID(), 3, 10000)),  // 주문 아이템
        LocalDateTime.now(),  // 예상 배송 날짜
        "Integration Test order note",  // 주문 노트
        "Integration Test details"  // 요청 세부사항
    );

    // When & Then: 실제 컨트롤러 호출
    mockMvc.perform(post("/api/v1/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(slackRequest)))
        .andExpect(status().isOk());
  }

  @Test
  void sendSlackMessage_ValidationFailure() throws Exception {
    // Given: 비어있는 SlackRequest
    SlackRequest invalidRequest = new SlackRequest("", "", "", "", "", List.of(), null, "", "");

    // When & Then
    mockMvc.perform(post("/api/v1/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void sendSlackMessage_InternalServerError() throws Exception {
    // Given: SlackService가 RuntimeException을 던지는 경우
    when(slackService.sendMessage(any(SlackRequest.class)))
        .thenThrow(new RuntimeException("Slack message failed!"));

    SlackRequest slackRequest = new SlackRequest(
        "YukCanDoIt",
        "Integration Test Message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 3, 10000)),
        LocalDateTime.now(),
        "Integration Test order note",
        "Integration Test details"
    );

    // When & Then
    mockMvc.perform(post("/api/v1/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(slackRequest)))
        .andExpect(status().isInternalServerError());
  }
}
