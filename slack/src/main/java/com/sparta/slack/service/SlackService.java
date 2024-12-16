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
  private final AICalculationService aiCalculationService;

  @Value("${slack.webhook.url}")
  private String slackWebhookUrl;

  public void setSlackWebhookUrl(String slackWebhookUrl) {
    this.slackWebhookUrl = slackWebhookUrl;
  }

  public String sendMessage(SlackRequest request, OrderRequest orderRequest) {
    if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
      throw new IllegalStateException("Slack Webhook URL is not configured.");
    }

    if (request == null || orderRequest == null) {
      throw new IllegalArgumentException("SlackRequest and OrderRequest must not be null.");
    }

    if (orderRequest.getOrderItems() == null || orderRequest.getOrderItems().isEmpty()) {
      throw new IllegalArgumentException("OrderRequest must contain at least one OrderItem.");
    }

    String finalDeadline = aiCalculationService.calculateDeadline(orderRequest);

    String orderItemsInfo = orderRequest.getOrderItems().stream()
        .map(item -> String.format("상품 ID: %s, 수량: %d, 단가: %d",
            item.productId(), item.quantity(), item.pricePerUnit()))
        .collect(Collectors.joining("\n"));

    String ordererInfo = String.format("주문 공급자 ID: %s, 수령인 ID: %s, 허브 ID: %s",
        orderRequest.supplierId(), orderRequest.receiverId(), orderRequest.hubId());

    String requestDetails = orderRequest.requestDetails();

    String slackMessage = String.format(
        "📦 *주문 상세 정보*\n\n" +
            "• *주문 번호*: %s\n" +
            "• *주문자 정보*: %s\n" +
            "• *상품 정보*: \n%s\n" +
            "• *요청 사항*: %s\n" +
            "• *발송 시한*: %s",
        orderRequest.supplierId(),
        ordererInfo,
        orderItemsInfo,
        requestDetails,
        finalDeadline
    );

    String response;
    try {
      response = restTemplate.postForObject(slackWebhookUrl, Map.of("text", slackMessage), String.class);
    } catch (Exception e) {
      throw new RuntimeException("Error sending message to Slack: " + e.getMessage());
    }

    SlackMessage message = SlackMessage.builder()
        .channel(request.channel())
        .message(slackMessage)
        .timestamp(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .build();

    slackMessageRepository.save(message);

    return response;
  }
}
