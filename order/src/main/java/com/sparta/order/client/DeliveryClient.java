package com.sparta.order.client;

import com.sparta.order.dto.OrderRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DeliveryClient {

  private final RestTemplate restTemplate;

  public DeliveryClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // 배송 생성 요청 메서드
  public UUID createDelivery(UUID orderId, OrderRequest orderRequest) {
    String deliveryServiceUrl = "http://delivery-service/api/deliveries";

    Map<String, Object> deliveryData = Map.of(
        "sourceHubId", orderRequest.getSourceHub(),
        "companyAddress", "Company Address",
        "recipient", "Recipient Name",
        "recipientSlackAccount", "SlackAccount",
        "dispatchDeadline", LocalDateTime.now().plusDays(1),
        "orderId", orderId, // 저장된 orderId 전달
        "companyId", orderRequest.getReceiverId()
    );

    ResponseEntity<UUID> response = restTemplate.postForEntity(deliveryServiceUrl, deliveryData, UUID.class);

    if (response.getStatusCode() == HttpStatus.OK) {
      return response.getBody();
    } else {
      throw new RuntimeException("배송 생성 실패: " + response.getStatusCode());
    }
  }
}
