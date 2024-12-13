package com.sparta.slack.service;

import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderRequest;
import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.repository.SlackMessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class SlackServiceTest {

  @Autowired
  private SlackService slackService;

  @MockBean
  private AICalculationService aiCalculationService;  // AICalculationService를 MockBean으로 주입

  @MockBean
  private SlackMessageRepository slackMessageRepository;  // SlackMessageRepository를 MockBean으로 주입

  @Test
  void sendMessage_Success() {
    // Given: SlackRequest 생성
    SlackRequest slackRequest = new SlackRequest(
        "YukCanDoIt",  // 채널 이름
        "Test Message",  // 메시지
        UUID.randomUUID().toString(),  // 공급자 ID
        UUID.randomUUID().toString(),  // 수신자 ID
        UUID.randomUUID().toString(),  // 허브 ID
        List.of(new OrderItemRequest(UUID.randomUUID(), 10, 100)), // UUID로 수정
        LocalDateTime.now(),  // 예상 배송 날짜
        "Order Note",  // 주문 노트
        "Request Details"  // 요청 세부사항
    );

    OrderRequest orderRequest = new OrderRequest(
        UUID.randomUUID(),  // 공급자 ID
        UUID.randomUUID(),  // 수신자 ID
        UUID.randomUUID(),  // 허브 ID
        List.of(new OrderItemRequest(UUID.randomUUID(), 10, 100)), // UUID로 수정
        LocalDateTime.now(),  // 예상 배송 날짜
        "Order Note",  // 주문 노트
        "Request Details"  // 요청 세부사항
    );

    // When: AICalculationService의 calculateDeadline() 메서드 모의
    when(aiCalculationService.calculateDeadline(any(OrderRequest.class))).thenReturn("2024-12-14 15:00");

    // SlackMessageRepository의 save() 메서드 모의
    when(slackMessageRepository.save(any())).thenReturn(null);

    // When: SlackService 호출
    String response = slackService.sendMessage(slackRequest, orderRequest);

    // Then: 응답이 null이 아니고 성공 메시지인지 확인
    assert response != null && response.contains("ok");
  }
}
