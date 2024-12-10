package com.sparta.delivery.entity;

import com.sparta.delivery.global.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
    @Column(updatable = false, nullable = false)
    private UUID deliveryId;

    @NotNull
    private UUID sourceHubId;

    @NotBlank
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryStatusEnum status = DeliveryStatusEnum.HUB_WAIT;

    @NotNull
    private String companyAddress;

    @NotBlank
    private String recipient;

    @NotBlank
    private String recipientSlackAccount;

    @NotNull
    private LocalDateTime dispatchDeadline;

    private Integer totalSequence;

    private Integer currentSeq;

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID companyId;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryRecords> deliveryRecords = new ArrayList<>();

}
