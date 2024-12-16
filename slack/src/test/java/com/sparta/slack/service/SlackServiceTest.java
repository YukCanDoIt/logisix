package com.sparta.slack.service;

import com.sparta.slack.domain.SlackMessage;
import com.sparta.slack.dto.OrderItemRequest;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.repository.SlackMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "slack.webhook.url=https://hooks.slack.com/services/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"
})
class SlackServiceTest {

  @Autowired
  private SlackService slackService;

  @Autowired
  private SlackMessageRepository slackMessageRepository;

  @MockBean
  private AICalculationService aiCalculationService;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @BeforeEach
  void setUp() {
    slackMessageRepository.deleteAll();
  }

  @Test
  void sendMessage_WithNullSlackRequest_ShouldThrowException() {
    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      slackService.sendMessage(null);
    });
    assertThat(exception.getMessage()).isEqualTo("SlackRequest 객체가 null일 수 없습니다.");
  }

  @Test
  void sendMessage_WithEmptyOrderItems_ShouldThrowException() {
    // Given
    SlackRequest slackRequest = new SlackRequest(
        "TestChannel",
        "Test Message",
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(), // 빈 리스트
        LocalDateTime.now(),
        "Order Note",
        "Request Details"
    );

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      slackService.sendMessage(slackRequest);
    });
    assertThat(exception.getMessage()).isEqualTo("주문 항목이 비어 있습니다.");
  }

  @Test
  void sendMessage_WithInvalidDeadline_ShouldThrowException() {
    // Given
    SlackRequest slackRequest = createSlackRequest();
    when(aiCalculationService.calculateDeadline(any(SlackRequest.class)))
        .thenReturn("invalid-deadline"); // 잘못된 날짜 포맷

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      slackService.sendMessage(slackRequest);
    });
    assertThat(exception.getMessage()).contains("유효하지 않은 날짜 형식");
  }

  @Test
  void sendMessage_Success_WithDynamicDeadline() {
    // Given
    SlackRequest slackRequest = createSlackRequest();
    String dynamicDeadline = LocalDateTime.now().plusDays(1).format(DATE_FORMATTER);

    when(aiCalculationService.calculateDeadline(any(SlackRequest.class)))
        .thenReturn(dynamicDeadline);

    // When
    String response = slackService.sendMessage(slackRequest);

    // Then
    assertThat(response).isNotNull();
    SlackMessage savedMessage = slackMessageRepository.findAll().get(0);
    assertThat(savedMessage.getTimestamp()).isEqualTo(LocalDateTime.parse(dynamicDeadline, DATE_FORMATTER));
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
}
