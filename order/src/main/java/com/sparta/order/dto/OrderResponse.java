package com.sparta.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.order.domain.OrderStatus;

import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID orderId,
    UUID supplierId,
    UUID receiverId,
    UUID hubId,
    List<OrderItemResponse> orderItems,
    String orderNote,
    OrderStatus status,
    UUID deliveryId,
    String requestDetails
) {

  // JSON 직렬화를 위한 전체 수량 계산
  @JsonProperty("quantity")
  public int quantity() {
    return orderItems.stream()
        .mapToInt(OrderItemResponse::quantity)
        .sum();
  }
}
