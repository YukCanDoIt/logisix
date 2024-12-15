package com.sparta.slack.service;

import com.sparta.order.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AICalculationService {

  private final AIClientService aiClientService;

  public String calculateDeadline(OrderRequest orderRequest) {
    // 요청 데이터 생성
    Map<String, Object> payload = Map.of(
        "orderItems", orderRequest.getOrderItems(),
        "expectedDeliveryDate", orderRequest.expectedDeliveryDate(),
        "supplierId", orderRequest.supplierId(),
        "receiverId", orderRequest.receiverId(),
        "hubId", orderRequest.hubId(),
        "orderNote", orderRequest.orderNote(),
        "requestDetails", orderRequest.requestDetails()
    );

    // 공통 서비스 호출
    return aiClientService.sendRequest(payload, "/calculateDeadline");
  }
}
