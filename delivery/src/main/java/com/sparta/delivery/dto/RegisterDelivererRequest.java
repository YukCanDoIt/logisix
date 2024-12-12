package com.sparta.delivery.dto;

import com.sparta.delivery.entity.DelivererTypeEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterDelivererRequest(
        @NotNull Long delivererId,
        @NotNull UUID hubId,
        @NotNull DelivererTypeEnum type
) {

}
