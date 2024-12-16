package com.sparta.delivery.dto;

import com.sparta.delivery.entity.Delivery;
import com.sparta.delivery.entity.DeliveryStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID orderId,
        DeliveryStatusEnum status,
        UUID sourceHubId,
        UUID companyId,
        String companyAddress,
        Integer totalSeq,
        Integer inProgressSeq,

        LocalDateTime dispatchDeadline
) {

    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getOrderId(),
                delivery.getStatus(),
                delivery.getSourceHubId(),
                delivery.getCompanyId(),
                delivery.getCompanyAddress(),
                delivery.getTotalSequence(),
                delivery.getCurrentSeq(),
                delivery.getDispatchDeadline()
        );
    }
}
