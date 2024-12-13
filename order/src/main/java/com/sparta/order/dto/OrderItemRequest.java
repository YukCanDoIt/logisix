package com.sparta.order.dto;

import java.util.UUID;

public record OrderItemRequest(
    UUID productId,  // 제품 ID
    int quantity,    // 수량
    int pricePerUnit // 단가
) {}
