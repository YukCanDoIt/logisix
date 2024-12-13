package com.sparta.slack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderRequest;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.repository.SlackMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SlackServiceTest {

  @Mock
  private SlackMessageRepository slackMessageRepository;

  @Mock
  private AICalculationService aiCalculationService;

  @InjectMocks
  private SlackService slackService;

  @BeforeEach
  public void setUp() {
    // Mock 객체 생성
    RestTemplate restTemplate = new RestTemplate();
    slackService = new SlackService(slackMessageRepository, restTemplate, aiCalculationService);

    // Webhook URL 설정
    slackService.setSlackWebhookUrl("https://hooks.slack.com/services/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");
  }

  @Test
  public void testSendMessage() {
    // SlackRequest 객체 생성
    SlackRequest request = new SlackRequest();
    request.setChannel("#general");
    request.setText("This is a test message from Slack!");

    // OrderItemRequest 객체 생성 (상품 정보)
    OrderItemRequest itemRequest = new OrderItemRequest(
        UUID.randomUUID(),  // 상품 ID
        10,  // 수량
        500  // 단가
    );

    // OrderRequest 객체 생성 (테스트용 예시)
    OrderRequest orderRequest = new OrderRequest(
        UUID.randomUUID(),  // 공급자 ID
        UUID.randomUUID(),  // 수신자 ID
        UUID.randomUUID(),  // 허브 ID
        List.of(itemRequest),  // 주문 아이템 (하나의 주문 아이템 추가)
        LocalDateTime.now(),  // 예상 배송 날짜
        "Test order note",  // 주문 노트
        "Test request details"  // 요청 세부사항
    );

    // AI의 결과값을 Mock
    when(aiCalculationService.calculateDeadline(any(OrderRequest.class))).thenReturn("2024-12-20 15:00:00");

    // 메시지 전송 테스트
    String response = slackService.sendMessage(request, orderRequest);

    // Slack에서 실제 응답 중 "ok"와 비교
    assertEquals("Message sent successfully!", response);

    // verify 메시지가 SlackMessageRepository에 저장되었는지 확인
    verify(slackMessageRepository, times(1)).save(any());
  }
}
