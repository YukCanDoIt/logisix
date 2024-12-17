package com.sparta.slack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    when(slackService.sendMessage(any(SlackRequest.class)))
        .thenReturn("Message sent successfully!");

    // When & Then
    mockMvc.perform(post("/api/v1/slack/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
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

  private SlackRequest createSlackRequest() {
    return new SlackRequest(
        "TestChannel",
        "Test Message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(new com.sparta.slack.dto.OrderItemRequest(UUID.randomUUID(), 2, 5000)),
        LocalDateTime.now(),
        "Test Note",
        "Test Details"
    );
  }
}
