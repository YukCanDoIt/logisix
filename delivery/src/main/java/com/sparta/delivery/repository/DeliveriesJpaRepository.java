package com.sparta.delivery.repository;

import com.sparta.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveriesJpaRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByDeliveryId(UUID deliveryId);
}
