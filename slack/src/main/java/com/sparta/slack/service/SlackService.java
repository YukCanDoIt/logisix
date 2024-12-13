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

  // Setter 추가
  public void setSlackWebhookUrl(String slackWebhookUrl) {
    this.slackWebhookUrl = slackWebhookUrl;
  }

  // 슬랙 메시지 발송 메서드
  public String sendMessage(SlackRequest request, OrderRequest orderRequest) {
    if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
      throw new IllegalStateException("Slack Webhook URL is not configured.");
    }

    // AI로부터 발송 시한 계산
    String finalDeadline = aiCalculationService.calculateDeadline(orderRequest);

    // 주문 아이템 정보를 처리하기 위한 문자열 포맷
    String orderItemsInfo = orderRequest.getOrderItems().stream()
        .map(item -> String.format("상품 ID: %s, 수량: %d, 단가: %d",
            item.productId(), item.quantity(), item.pricePerUnit()))
        .collect(Collectors.joining("\n"));

    // 주문자 정보 (여기서 supplierId, receiverId 등을 활용하여 주문자 정보를 추가할 수 있습니다)
    String ordererInfo = String.format("주문 공급자 ID: %s, 수령인 ID: %s, 허브 ID: %s",
        orderRequest.supplierId(), orderRequest.receiverId(), orderRequest.hubId());

    // 요청 사항
    String requestDetails = orderRequest.requestDetails();

    // 최종 메시지 포맷
    String slackMessage = String.format(
        "📦 *주문 상세 정보*\n\n" +
            "• *주문 번호*: %s\n" +
            "• *주문자 정보*: %s\n" +  // 주문자 정보 (supplierId, receiverId, hubId 포함)
            "• *상품 정보*: \n%s\n" +  // 상품 정보
            "• *요청 사항*: %s\n" +
            "• *발송 시한*: %s",
        orderRequest.supplierId(),  // 주문 번호 (supplierId)
        ordererInfo,  // 주문자 정보
        orderItemsInfo,  // 상품 정보
        requestDetails,  // 요청 사항
        finalDeadline  // 최종 발송 시한
    );

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
