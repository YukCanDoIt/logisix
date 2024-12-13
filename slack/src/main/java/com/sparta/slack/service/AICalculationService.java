package com.sparta.slack.service;

import com.sparta.order.dto.OrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AICalculationService {

  private final RestTemplate restTemplate;

  @Value("${ai.api.url}")
  private String aiApiUrl;

  public AICalculationService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String calculateDeadline(OrderRequest orderRequest) {
    Map<String, Object> requestPayload = Map.of(
        "orderItems", orderRequest.getOrderItems(),
        "expectedDeliveryDate", orderRequest.expectedDeliveryDate(),
        "supplierId", orderRequest.supplierId(),
        "receiverId", orderRequest.receiverId(),
        "hubId", orderRequest.hubId(),
        "orderNote", orderRequest.orderNote(),
        "requestDetails", orderRequest.requestDetails()
    );

    String response = restTemplate.postForObject(aiApiUrl, requestPayload, String.class);
    return response != null ? response : "AI 처리 실패";
  }
}
