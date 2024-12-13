package com.sparta.slack.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.order.dto.OrderRequest;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.service.SlackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class SlackControllerTest {

  private MockMvc mockMvc;

  @Mock
  private SlackService slackService;

  @InjectMocks
  private SlackController slackController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(slackController).build();
  }

  @Test
  void sendSlackMessage_Success() throws Exception {
    // Given
    SlackRequest slackRequest = new SlackRequest("general", "Test message");

    // OrderRequest 생성 (테스트용)
    OrderRequest orderRequest = new OrderRequest(
        UUID.randomUUID(),  // supplierId
        UUID.randomUUID(),  // receiverId
        UUID.randomUUID(),  // hubId
        List.of(),  // 빈 주문 아이템 리스트
        LocalDateTime.now(),  // 예상 배송 날짜
        "Test order note",  // 주문 노트
        "Test request details"  // 요청 세부사항
    );

    // SlackService의 sendMessage 메서드가 호출되었을 때 반환할 값 설정
    when(slackService.sendMessage(any(SlackRequest.class), any(OrderRequest.class)))
        .thenReturn("Message sent successfully!");

    // When & Then
    mockMvc.perform(post("/api/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(slackRequest)))  // SlackRequest만 전달
        .andExpect(status().isOk());
  }
}
