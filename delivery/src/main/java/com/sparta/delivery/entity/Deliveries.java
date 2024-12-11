package com.sparta.delivery.entity;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "p_deliveries")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Deliveries extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID deliveryId;

    @Column(nullable = false)
    private UUID sourceHubId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryStatusEnum status = DeliveryStatusEnum.HUB_WAIT;

    @Column(nullable = false)
    private String companyAddress;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String recipientSlackAccount;

    @Column(nullable = false)
    private LocalDateTime dispatchDeadline;

    private Integer totalSequence;

    private Integer currentSeq;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID companyId;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryRecords> deliveryRecords = new ArrayList<>();

}
