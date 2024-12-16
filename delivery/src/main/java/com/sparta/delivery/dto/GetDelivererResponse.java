package com.sparta.delivery.dto;

import com.sparta.delivery.entity.DelivererStatusEnum;
import com.sparta.delivery.entity.DelivererTypeEnum;
import com.sparta.delivery.entity.Deliverer;

import java.util.UUID;

public record GetDelivererResponse(
    Long delivererId,
    UUID hubId,
    DelivererTypeEnum type,
    DelivererStatusEnum status
) {

    public static GetDelivererResponse from(Deliverer deliverer) {
        return new GetDelivererResponse(
                deliverer.getDelivererId(),
                deliverer.getHubId(),
                deliverer.getType(),
                deliverer.getStatus()
        );
    }
}
