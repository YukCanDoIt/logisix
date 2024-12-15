package com.sparta.slack.service;

import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderRequest;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.repository.SlackMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "eureka.client.enable=false",
    "spring.cloud.discovery.enabled=false"
})
class SlackServiceTest {

  @Autowired
  private SlackService slackService;

  @MockBean
  private AICalculationService aiCalculationService;

  @MockBean
  private SlackMessageRepository slackMessageRepository;

  @Test
  void sendMessage_Success_WithDynamicTime() {
    SlackRequest slackRequest = new SlackRequest(
        "TestChannel",
        "Test Message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 5, 1000)),
        LocalDateTime.now(),
        "Test Order Note",
        "Test Request Details"
    );

    OrderRequest orderRequest = new OrderRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 5, 1000)),
        LocalDateTime.now(),
        "Test Order Note",
        "Test Request Details"
    );

    String dynamicDeadline = LocalDateTime.now()
        .plusDays(1)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

    when(aiCalculationService.calculateDeadline(any(OrderRequest.class)))
        .thenReturn(dynamicDeadline);
    when(slackMessageRepository.save(any())).thenReturn(null);

    String response = slackService.sendMessage(slackRequest, orderRequest);

    assertNotNull(response, "응답이 null이어서는 안 됩니다.");
  }

  @Test
  void sendMessage_Failure_WhenAICalculationFails() {
    SlackRequest slackRequest = new SlackRequest(
        "TestChannel",
        "Test Message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 5, 1000)),
        LocalDateTime.now(),
        "Test Order Note",
        "Test Request Details"
    );

    OrderRequest orderRequest = new OrderRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 5, 1000)),
        LocalDateTime.now(),
        "Test Order Note",
        "Test Request Details"
    );

    when(aiCalculationService.calculateDeadline(any(OrderRequest.class)))
        .thenThrow(new RuntimeException("AI 서비스 오류"));

    Exception exception = assertThrows(RuntimeException.class, () -> {
      slackService.sendMessage(slackRequest, orderRequest);
    });

    assertTrue(exception.getMessage().contains("AI 서비스 오류"));
  }

  @Test
  void sendMessage_SavesSlackMessage() {
    SlackRequest slackRequest = new SlackRequest(
        "TestChannel",
        "Test Message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 5, 1000)),
        LocalDateTime.now(),
        "Test Order Note",
        "Test Request Details"
    );

    OrderRequest orderRequest = new OrderRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(new OrderItemRequest(UUID.randomUUID(), 5, 1000)),
        LocalDateTime.now(),
        "Test Order Note",
        "Test Request Details"
    );

    when(aiCalculationService.calculateDeadline(any(OrderRequest.class)))
        .thenReturn("2024-12-14 15:00");
    when(slackMessageRepository.save(any())).thenReturn(null);

    slackService.sendMessage(slackRequest, orderRequest);

    verify(slackMessageRepository).save(any());
  }

  @Test
  void sendMessage_Failure_WhenSlackRequestIsEmpty() {
    SlackRequest slackRequest = null;
    OrderRequest orderRequest = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      slackService.sendMessage(slackRequest, orderRequest);
    });

    assertTrue(exception.getMessage().contains("SlackRequest and OrderRequest must not be null"));
  }
}
