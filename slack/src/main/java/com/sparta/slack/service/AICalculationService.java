package com.sparta.slack.service;

import com.sparta.slack.dto.OrderItemRequest;
import com.sparta.slack.dto.SlackRequest;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AICalculationService {

  private final AIClientService aiClientService;

  public String calculateDeadline(SlackRequest request) {
    // 요청 데이터 생성
    Map<String, Object> payload = Map.of(
        "orderItems", mapOrderItems(request.orderItems()),
        "expectedDeliveryDate", request.expectedDeliveryDate(),
        "supplierId", request.orderSupplierId(),
        "receiverId", request.orderReceiverId(),
        "hubId", request.orderHubId(),
        "orderNote", request.orderNote(),
        "requestDetails", request.requestDetails()
    );

    // 공통 서비스 호출
    return aiClientService.sendRequest(payload, "/calculateDeadline");
  }

  private List<Map<String, Object>> mapOrderItems(List<OrderItemRequest> orderItems) {
    return orderItems.stream()
        .map(item -> {
          Map<String, Object> map = new HashMap<>();
          map.put("productId", item.productId() != null ? item.productId().toString() : null); // null 안전 처리
          map.put("quantity", item.quantity());
          map.put("pricePerUnit", item.pricePerUnit());
          return map;
        })
        .toList();
  }

}
