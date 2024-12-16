package com.sparta.delivery.dto;

import com.sparta.delivery.entity.Deliveries;
import com.sparta.delivery.entity.DeliveryRecords;

import java.util.List;

public record GetDeliveryResponse(
    DeliveryResponse delivery,
    List<DeliveryRecordsResponse> details
) {
    public static GetDeliveryResponse create(Deliveries delivery, List<DeliveryRecords> deliveryRecordsList) {
        DeliveryResponse deliveryResponse = DeliveryResponse.from(delivery);
        List<DeliveryRecordsResponse> details = deliveryRecordsList.stream()
                .map(DeliveryRecordsResponse::from)
                .toList();
        return new GetDeliveryResponse(deliveryResponse, details);
    }

}
