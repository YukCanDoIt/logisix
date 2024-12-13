package com.sparta.order.dto;

import com.sparta.order.domain.OrderStatus;
import java.util.UUID;

public record OrderResponse(
    UUID orderId,
    UUID supplierId,
    UUID receiverId,
    UUID productId,
    int quantity,
    String requestDetails,
    UUID deliveryId,
    boolean isDelete,
    OrderStatus status
) {}
