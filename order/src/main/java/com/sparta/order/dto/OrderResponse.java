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

  // JSON 응답에 포함되도록 @JsonProperty 추가
  @JsonProperty("quantity")
  public int quantity() {
    return orderItems.stream()
        .mapToInt(OrderItemResponse::quantity)  // quantity 합산
        .sum();
  }

  @JsonProperty("deliveryId")
  public UUID deliveryId() {
    return this.deliveryId;
  }

  @JsonProperty("orderNote")
  public String orderNote() {
    return this.orderNote;
  }

  @JsonProperty("requestDetails")
  public String requestDetails() {
    return this.requestDetails;
  }
}
