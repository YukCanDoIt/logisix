package com.sparta.slack.controller;

import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.dto.SlackResponse;
import com.sparta.slack.service.SlackService;
import com.sparta.order.dto.OrderRequest; // OrderRequest 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/slack")
public class SlackController {

  private final SlackService slackService;

  public SlackController(SlackService slackService) {
    this.slackService = slackService;
  }

  @PostMapping("/send")
  public ResponseEntity<?> sendSlackMessage(@RequestBody SlackRequest request) {
    try {
      // 클라이언트에서 받은 주문 데이터로 OrderRequest 생성
      OrderRequest orderRequest = createOrderRequest(request);

      // SlackService의 sendMessage 메서드 호출
      String response = slackService.sendMessage(request, orderRequest);

      return ResponseEntity.ok(new SlackResponse(true, response, request.channel()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new SlackResponse(false, e.getMessage(), request.channel()));
    }
  }

  // UUID를 안전하게 파싱하는 메서드
  private UUID parseUUID(String uuidString) {
    try {
      return UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid UUID format: " + uuidString);
    }
  }

  // SlackRequest에서 OrderRequest 객체를 생성하는 메서드
  private OrderRequest createOrderRequest(SlackRequest request) {
    return new OrderRequest(
        parseUUID(request.orderSupplierId()), // 클라이언트로부터 받은 공급자 ID
        parseUUID(request.orderReceiverId()), // 클라이언트로부터 받은 수신자 ID
        parseUUID(request.orderHubId()), // 클라이언트로부터 받은 허브 ID
        request.orderItems(), // 클라이언트로부터 받은 주문 아이템 리스트
        request.expectedDeliveryDate(), // 클라이언트로부터 받은 예상 배송 날짜
        request.orderNote(), // 클라이언트로부터 받은 주문 노트
        request.requestDetails() // 클라이언트로부터 받은 요청 세부사항
    );
  }
}
