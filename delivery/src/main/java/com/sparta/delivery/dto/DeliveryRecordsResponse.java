package com.sparta.delivery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.delivery.entity.DeliveryRecords;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;


public record DeliveryRecordsResponse(
        UUID deliveryRecordId,
        UUID departures,
        UUID arrival,
        Integer seq,
        String status,
        BigDecimal estimatedDist,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        String estimatedTime,
        BigDecimal actualDist,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        String actualTime,
        Long delivererId
) {
    public static DeliveryRecordsResponse from(DeliveryRecords deliveryRecords) {
        return new DeliveryRecordsResponse(
                deliveryRecords.getDeliveryRecordId(),
                deliveryRecords.getDepartures(),
                deliveryRecords.getArrival(),
                deliveryRecords.getSequence(),
                deliveryRecords.getStatus().name(),
                deliveryRecords.getEstimatedDist(),
                formatDuration(deliveryRecords.getEstimatedTime()),
                deliveryRecords.getActualDist(),
                formatDuration(deliveryRecords.getActualTime()),
                deliveryRecords.getDeliverer() != null ? deliveryRecords.getDeliverer().getDelivererId() : null
        );
    }

    private static String formatDuration(Duration duration) {
        if (duration == null) {
            return null;
        }
        LocalTime time = LocalTime.ofSecondOfDay(duration.getSeconds());
        return time.toString();
    }
}
