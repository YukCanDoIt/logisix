package com.sparta.delivery.entity;

import com.sparta.delivery.common.BaseEntity;
import com.sparta.delivery.util.DurationToIntervalConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Entity(name = "p_delivery_records")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class DeliveryRecords extends BaseEntity {

    @Id
    @UuidGenerator
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID deliveryRecordId;

    @Column(nullable = false)
    private UUID departures;

    @Column(nullable = false)
    private UUID arrival;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryRecordsStatusEnum status = DeliveryRecordsStatusEnum.WAIT;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    @Convert(converter = DurationToIntervalConverter.class)
    private Duration estimatedTime;

    @Column(nullable = false)
    private BigDecimal estimatedDist;

    @Convert(converter = DurationToIntervalConverter.class)
    private Duration actualTime;

    private BigDecimal actualDist;

    @ManyToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private Deliveries delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverer_id")
    private Deliverers deliverer;

    public static DeliveryRecords create(
            UUID departures,
            UUID arrival,
            Integer sequence,
            Duration estimatedTime,
            BigDecimal estimatedDist,
            Deliveries delivery
    ) {
        return DeliveryRecords.builder()
                .departures(departures)
                .arrival(arrival)
                .status(DeliveryRecordsStatusEnum.WAIT)
                .sequence(sequence)
                .estimatedTime(estimatedTime)
                .estimatedDist(estimatedDist)
                .delivery(delivery)
                .build();
    }

    public void changeDeliverer(Deliverers deliverer) {
        this.deliverer = deliverer;
    }

}
