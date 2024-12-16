package com.sparta.delivery.repository;

import com.sparta.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DeliveriesJpaRepository extends JpaRepository<Delivery, UUID> {
    @Query("SELECT d FROM p_deliveries d WHERE d.deliveryId = :deliveryId AND d.isDeleted = false")
    Optional<Delivery> findByDeliveryId(UUID deliveryId);
}
