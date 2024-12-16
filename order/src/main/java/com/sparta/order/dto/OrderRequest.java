package com.sparta.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
    long supplierId,
    long receiverId,
    UUID hubId,
    List<OrderItemRequest> orderItems,
    LocalDateTime expectedDeliveryDate,
    String orderNote,
    String requestDetails
) {
  // orderItems 반환 메서드
  public List<OrderItemRequest> getOrderItems() {
    return this.orderItems;
  }
}
