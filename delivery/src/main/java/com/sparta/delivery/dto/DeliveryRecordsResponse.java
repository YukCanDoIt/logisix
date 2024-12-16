package com.sparta.delivery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.delivery.entity.DeliveryRecord;

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
    public static DeliveryRecordsResponse from(DeliveryRecord deliveryRecord) {
        return new DeliveryRecordsResponse(
                deliveryRecord.getDeliveryRecordId(),
                deliveryRecord.getDepartures(),
                deliveryRecord.getArrival(),
                deliveryRecord.getSequence(),
                deliveryRecord.getStatus().name(),
                deliveryRecord.getEstimatedDist(),
                formatDuration(deliveryRecord.getEstimatedTime()),
                deliveryRecord.getActualDist(),
                formatDuration(deliveryRecord.getActualTime()),
                deliveryRecord.getDeliverer() != null ? deliveryRecord.getDeliverer().getDelivererId() : null
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
