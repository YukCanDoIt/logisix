package com.sparta.delivery.dto;

import com.sparta.delivery.entity.DelivererStatusEnum;
import com.sparta.delivery.entity.DelivererTypeEnum;
import com.sparta.delivery.entity.Deliverers;

import java.util.UUID;

public record GetDelivererResponse(
    Long delivererId,
    UUID hubId,
    DelivererTypeEnum type,
    DelivererStatusEnum status
) {

    public static GetDelivererResponse from(Deliverers deliverers) {
        return new GetDelivererResponse(
                deliverers.getDelivererId(),
                deliverers.getHubId(),
                deliverers.getType(),
                deliverers.getStatus()
        );
    }
}
