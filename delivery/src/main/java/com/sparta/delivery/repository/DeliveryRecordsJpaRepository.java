package com.sparta.delivery.repository;

import com.sparta.delivery.entity.DeliveryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRecordsJpaRepository extends JpaRepository<DeliveryRecord, UUID> {
    @Query("SELECT dr FROM p_delivery_records dr WHERE dr.delivery.deliveryId = :deliveryId AND dr.isDeleted = false")
    List<DeliveryRecord> findAllByDelivery_DeliveryId(UUID deliveryId);

    @Query("SELECT dr FROM p_delivery_records dr WHERE dr.delivery.deliveryId = :deliveryId AND dr.sequence = :sequence AND dr.isDeleted = false")
    Optional<DeliveryRecord> findByDeliveryIdAndSequence(UUID deliveryId, int nextSequence);
}
