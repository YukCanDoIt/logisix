package com.sparta.slack.dto;

import java.util.UUID;

public record OrderItemRequest(
    UUID productId,
    int quantity,
    int pricePerUnit
) {}
