package com.sparta.delivery.repository;

import com.sparta.delivery.entity.DeliveryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRecordsJpaRepository extends JpaRepository<DeliveryRecord, UUID> {
    List<DeliveryRecord> findAllByDelivery_DeliveryId(UUID deliveryId);

}
