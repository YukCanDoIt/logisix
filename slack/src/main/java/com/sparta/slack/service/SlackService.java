package com.sparta.slack.service;

import com.sparta.slack.domain.SlackMessage;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.exception.ErrorCode;
import com.sparta.slack.exception.LogisixException;
import com.sparta.slack.repository.SlackMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlackService {

  private final SlackMessageRepository slackMessageRepository;
  private final AICalculationService aiCalculationService;
  private final RestTemplate restTemplate;

  @Value("${slack.webhook.url}")
  private String slackWebhookUrl;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public String sendMessage(SlackRequest request) {
    // 요청 검증
    if (request == null) {
      throw new LogisixException(ErrorCode.INVALID_REQUEST_DATA);
    }
    if (request.orderItems() == null || request.orderItems().isEmpty()) {
      throw new LogisixException(ErrorCode.INVALID_REQUEST_DATA);
    }

    // AI 서비스로부터 마감 시간 계산
    String calculatedDeadline = aiCalculationService.calculateDeadline(request);
    if (calculatedDeadline == null || calculatedDeadline.isBlank()) {
      throw new LogisixException(ErrorCode.INVALID_REQUEST_DATA);
    }

    LocalDateTime deadline;
    try {
      deadline = LocalDateTime.parse(calculatedDeadline, DATE_FORMATTER);
    } catch (Exception e) {
      throw new LogisixException(ErrorCode.INVALID_REQUEST_DATA);
    }

    // 주문 아이템 정보 생성
    String orderItemsInfo = request.orderItems().stream()
        .map(item -> String.format("상품 ID: %s, 수량: %d, 단가: %d",
            item.productId(), item.quantity(), item.pricePerUnit()))
        .collect(Collectors.joining("\n"));

    // Slack 메시지 생성
    String slackMessage = String.format(
        "📦 *주문 상세 정보*\n\n" +
            "• *주문 공급자 ID*: %s\n" +
            "• *수령인 ID*: %s\n" +
            "• *허브 ID*: %s\n" +
            "• *상품 정보*: \n%s\n" +
            "• *요청 사항*: %s\n" +
            "• *예상 배송일*: %s\n" +
            "• *마감 시간*: %s",
        request.orderSupplierId(),
        request.orderReceiverId(),
        request.orderHubId(),
        orderItemsInfo,
        request.orderNote(),
        request.expectedDeliveryDate(),
        calculatedDeadline
    );

    // Slack 메시지 전송
    String response;
    try {
      response = restTemplate.postForObject(slackWebhookUrl, Map.of("text", slackMessage), String.class);
    } catch (Exception e) {
      throw new RuntimeException("Slack으로 메시지를 보내는 중 오류가 발생했습니다.: " + e.getMessage());
    }

    // Slack 메시지 저장
    SlackMessage message = SlackMessage.builder()
        .channel(request.channel())
        .message(slackMessage)
        .timestamp(deadline)
        .createdAt(LocalDateTime.now())
        .build();

    slackMessageRepository.save(message);

    return response;
  }
}
