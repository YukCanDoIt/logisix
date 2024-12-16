package com.sparta.delivery.repository;

import com.sparta.delivery.entity.Deliveries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveriesJpaRepository extends JpaRepository<Deliveries, UUID> {
    Optional<Deliveries> findByDeliveryId(UUID deliveryId);
}
