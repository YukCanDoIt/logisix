package com.sparta.slack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.order.dto.OrderRequest;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.service.SlackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
  private SlackService slackService; // 필요한 의존성을 Mock으로 대체

  @Test
  void sendSlackMessage_Success() throws Exception {
    // Given: Mock SlackService
    when(slackService.sendMessage(any(SlackRequest.class), any(OrderRequest.class)))
        .thenReturn("Mock response from SlackService");

    // 실제 SlackRequest 생성
    SlackRequest slackRequest = new SlackRequest(
        "YukCanDoIt",  // 채널 이름
        "Integration Test Message",  // 메시지
        UUID.randomUUID().toString(),  // 공급자 ID
        UUID.randomUUID().toString(),  // 수신자 ID
        UUID.randomUUID().toString(),  // 허브 ID
        List.of(),  // 빈 주문 아이템 리스트
        LocalDateTime.now(),  // 예상 배송 날짜
        "Integration Test order note",  // 주문 노트
        "Integration Test details"  // 요청 세부사항
    );

    // When & Then: 실제 컨트롤러 호출
    mockMvc.perform(post("/api/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(slackRequest)))
        .andExpect(status().isOk());
  }
}
