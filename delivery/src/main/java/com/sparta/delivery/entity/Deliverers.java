package com.sparta.delivery.entity;

import com.sparta.delivery.global.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "p_deliverers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Deliverers extends BaseEntity {

    @Id
    private Long delivererId;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DelivererStatusEnum status = DelivererStatusEnum.WAIT;

    @NotNull
    private UUID hubId;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DelivererTypeEnum type;

    @OneToMany(mappedBy = "deliverer")
    private List<DeliveryRecords> deliveryRecords = new ArrayList<>();

}
