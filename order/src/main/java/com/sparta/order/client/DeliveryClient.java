package com.sparta.order.client;

import com.sparta.order.dto.OrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class DeliveryClient {

  private final RestTemplate restTemplate;

  public DeliveryClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public UUID createDelivery(UUID orderId, OrderRequest orderRequest) {
    String deliveryServiceUrl = "http://delivery-service/api/deliveries";

    Map<String, Object> deliveryData = Map.of(
        "hubId", orderRequest.hubId(),
        "orderItems", orderRequest.orderItems(),
        "expectedDeliveryDate", orderRequest.expectedDeliveryDate(),
        "orderNote", orderRequest.orderNote(),
        "orderId", orderId
    );

    ResponseEntity<UUID> response = restTemplate.postForEntity(deliveryServiceUrl, deliveryData, UUID.class);

    if (response.getStatusCode() == HttpStatus.OK) {
      return response.getBody();
    } else {
      throw new RuntimeException("배송 생성 실패: " + response.getStatusCode());
    }
  }
}
