package com.sparta.delivery.dto;

import com.sparta.delivery.entity.Delivery;
import com.sparta.delivery.entity.DeliveryRecord;

import java.util.List;

public record GetDeliveryResponse(
    DeliveryResponse delivery,
    List<DeliveryRecordsResponse> details
) {
    public static GetDeliveryResponse create(Delivery delivery, List<DeliveryRecord> deliveryRecordList) {
        DeliveryResponse deliveryResponse = DeliveryResponse.from(delivery);
        List<DeliveryRecordsResponse> details = deliveryRecordList.stream()
                .map(DeliveryRecordsResponse::from)
                .toList();
        return new GetDeliveryResponse(deliveryResponse, details);
    }

}
