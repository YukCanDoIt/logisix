package com.sparta.slack.service;

import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderRequest;
import com.sparta.slack.domain.SlackMessage;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.repository.SlackMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "eureka.client.enable=false",
    "spring.cloud.discovery.enabled=false"
})
@Transactional
class SlackServiceTest {

  @Autowired
  private SlackService slackService;

  @Autowired
  private SlackMessageRepository slackMessageRepository;

  @MockBean
  private AICalculationService aiCalculationService;

  @BeforeEach
  void setUp() {
    slackMessageRepository.deleteAll();
  }

  @Test
  void sendMessage_Success_WithDynamicTime() {
    // Given
    SlackRequest slackRequest = createSlackRequest();
    OrderRequest orderRequest = createOrderRequest();

    String dynamicDeadline = LocalDateTime.now().plusDays(1).toString();
    when(aiCalculationService.calculateDeadline(any(OrderRequest.class)))
        .thenReturn(dynamicDeadline);

    // When
    String response = slackService.sendMessage(slackRequest, orderRequest);

    // Then
    assertNotNull(response);
    List<SlackMessage> savedMessages = slackMessageRepository.findAll();
    assertThat(savedMessages).hasSize(1);
  }

  @Test
  void sendMessage_Failure_WhenAICalculationFails() {
    // Given
    SlackRequest slackRequest = createSlackRequest();
    OrderRequest orderRequest = createOrderRequest();

    when(aiCalculationService.calculateDeadline(any(OrderRequest.class)))
        .thenThrow(new RuntimeException("AI 서비스 오류"));

    // When & Then
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        slackService.sendMessage(slackRequest, orderRequest)
    );
    assertTrue(exception.getMessage().contains("AI 서비스 오류"));
  }

  @Test
  void sendMessage_SavesSlackMessage() {
    // Given
    SlackRequest slackRequest = createSlackRequest();
    OrderRequest orderRequest = createOrderRequest();

    when(aiCalculationService.calculateDeadline(any(OrderRequest.class)))
        .thenReturn("2024-12-14 15:00");

    // When
    slackService.sendMessage(slackRequest, orderRequest);

    // Then
    List<SlackMessage> savedMessages = slackMessageRepository.findAll();
    assertThat(savedMessages).hasSize(1);

    SlackMessage savedMessage = savedMessages.get(0);
    assertThat(savedMessage.getChannel()).isEqualTo(slackRequest.channel());
    assertThat(savedMessage.getMessage()).contains("2024-12-14 15:00");
  }

  @Test
  void sendMessage_Failure_WhenSlackRequestIsEmpty() {
    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        slackService.sendMessage(null, null)
    );
    assertTrue(exception.getMessage().contains("SlackRequest and OrderRequest must not be null"));
  }

  private SlackRequest createSlackRequest() {
    return new SlackRequest(
        "TestChannel",
        "Test Message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 2, 5000)),
        LocalDateTime.now(),
        "Order Note",
        "Request Details"
    );
  }

  private OrderRequest createOrderRequest() {
    return new OrderRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 2, 5000)),
        LocalDateTime.now(),
        "Order Note",
        "Request Details"
    );
  }
}
