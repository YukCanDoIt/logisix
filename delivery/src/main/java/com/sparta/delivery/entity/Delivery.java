package com.sparta.delivery.entity;

import com.sparta.delivery.common.BaseEntity;
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
@Setter
public class Delivery extends BaseEntity {

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

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer totalSequence;

    private Integer currentSeq;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID companyId;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryRecord> deliveryRecords = new ArrayList<>();

    public static Delivery create(
            UUID orderId,
            UUID sourceHubId,
            UUID companyId,
            String companyAddress,
            String recipient,
            String recipientSlackAccount
    ) {
        return Delivery.builder()
                .orderId(orderId)
                .sourceHubId(sourceHubId)
                .companyId(companyId)
                .status(DeliveryStatusEnum.HUB_WAIT)
                .companyAddress(companyAddress)
                .recipient(recipient)
                .recipientSlackAccount(recipientSlackAccount)
                .build();
    }

    public void setFirstDeliveryStatus(LocalDateTime startAt) {
        this.startAt = startAt;
        this.status = DeliveryStatusEnum.HUB_MOVE;
        this.currentSeq = 1;
    }

    public void setHubArrivedDeliveryStatus(LocalDateTime endAt) {
        this.endAt = endAt;
        this.status = DeliveryStatusEnum.HUB_ARRIVED;
        this.currentSeq = this.getTotalSequence()-2;
    }

}
