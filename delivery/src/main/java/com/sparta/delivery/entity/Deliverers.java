package com.sparta.delivery.entity;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DelivererStatusEnum status = DelivererStatusEnum.WAIT;

    @Column(nullable = false)
    private UUID hubId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DelivererTypeEnum type;

    @OneToMany(mappedBy = "deliverer")
    private List<DeliveryRecords> deliveryRecords = new ArrayList<>();

}
