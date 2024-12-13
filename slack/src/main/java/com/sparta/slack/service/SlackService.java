package com.sparta.slack.service;

import com.sparta.order.dto.OrderRequest;
import com.sparta.slack.domain.SlackMessage;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.repository.SlackMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlackService {

  private final SlackMessageRepository slackMessageRepository;
  private final RestTemplate restTemplate;

  @Value("${slack.webhook.url}")
  private String slackWebhookUrl;

  // Setter 추가
  public void setSlackWebhookUrl(String slackWebhookUrl) {
    this.slackWebhookUrl = slackWebhookUrl;
  }

  // 슬랙 메시지 발송 메서드
  public String sendMessage(SlackRequest request, OrderRequest orderRequest) {
    if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
      throw new IllegalStateException("Slack Webhook URL is not configured.");
    }

    // 주문 아이템 정보를 처리하기 위한 문자열 포맷
    String orderItemsInfo = orderRequest.getOrderItems().stream()
        .map(item -> String.format("상품 ID: %s, 수량: %d, 단가: %d",
            item.productId(), item.quantity(), item.pricePerUnit()))
        .collect(Collectors.joining("\n"));

    // 최종 메시지 포맷
    String slackMessage = String.format(
        "주문 번호: %s\n상품 정보: \n%s\n요청 사항: %s",
        orderRequest.supplierId(), orderItemsInfo, orderRequest.requestDetails());

    // 슬랙 메시지 발송
    String response;
    try {
      response = restTemplate.postForObject(slackWebhookUrl, Map.of("text", slackMessage), String.class);
    } catch (Exception e) {
      throw new RuntimeException("Error sending message to Slack: " + e.getMessage());
    }

    // 메시지 DB 저장
    SlackMessage message = SlackMessage.builder()
        .channel(request.channel())
        .message(slackMessage)
        .timestamp(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .build();

    slackMessageRepository.save(message);

    return response;  // 슬랙의 실제 응답 반환
  }
}
