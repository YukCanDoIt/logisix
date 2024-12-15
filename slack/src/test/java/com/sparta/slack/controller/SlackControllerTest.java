package com.sparta.slack.controller;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    objectMapper.findAndRegisterModules();
  }

  @Test
  void sendSlackMessage_Success() throws Exception {
    // Given
    SlackRequest request = createSlackRequest();

    when(slackService.sendMessage(any(SlackRequest.class), any(OrderRequest.class)))
        .thenReturn("Message sent successfully!");

    // When & Then
    mockMvc.perform(post("/api/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void sendSlackMessage_ValidationFailure() throws Exception {
    // Given: 비어있는 SlackRequest
    SlackRequest invalidRequest = new SlackRequest("", "", "", "", "", List.of(), null, "", "");

    // When & Then
    mockMvc.perform(post("/api/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void sendSlackMessage_InternalServerError() throws Exception {
    // Given
    SlackRequest request = createSlackRequest();

    when(slackService.sendMessage(any(SlackRequest.class), any(OrderRequest.class)))
        .thenThrow(new RuntimeException("Slack message failed!"));

    // When & Then
    mockMvc.perform(post("/api/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  private SlackRequest createSlackRequest() {
    return new SlackRequest(
        "general",
        "Test message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(),
        LocalDateTime.now(),
        "Test note",
        "Test details"
    );
  }
}
