package com.sparta.delivery.entity;

import com.sparta.delivery.global.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Entity(name = "p_delivery_records")
public class DeliveryRecords extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID deliveryRecordId;

    @NotNull
    private UUID departures;

    @NotNull
    private UUID arrival;

    @NotBlank
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryRecordsStatusEnum status = DeliveryRecordsStatusEnum.WAIT;

    @NotNull
    private Integer sequence;

    @NotNull
    private Duration estimatedTime;

    @NotNull
    private BigDecimal estimatedDist;

    private Duration actualTime;

    private BigDecimal actualDist;

    @ManyToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private Deliveries delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverer_id")
    private Deliverers deliverer;

}
