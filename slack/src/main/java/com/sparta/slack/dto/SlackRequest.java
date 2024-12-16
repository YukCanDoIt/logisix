package com.sparta.slack.dto;

import com.sparta.order.dto.OrderItemRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record SlackRequest(
    @NotEmpty(message = "Channel cannot be empty")
    @Size(max = 50, message = "Channel name must not exceed 50 characters")
    String channel,

    @NotEmpty(message = "Text cannot be empty")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    String text,

    String orderSupplierId,
    String orderReceiverId,
    String orderHubId,
    List<OrderItemRequest> orderItems,
    LocalDateTime expectedDeliveryDate,
    String orderNote,
    String requestDetails
) {}
