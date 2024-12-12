package com.sparta.delivery.dto;

import com.sparta.delivery.entity.DelivererTypeEnum;

import java.util.UUID;

public record UpdateDelivererRequest(
        UUID hubId,
        DelivererTypeEnum type
) {
}
