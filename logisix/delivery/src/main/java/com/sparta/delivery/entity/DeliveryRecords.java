package com.sparta.delivery.entity;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Entity(name = "p_delivery_records")
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
    private Duration estimatedTime;

    @Column(nullable = false)
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
