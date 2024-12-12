package com.sparta.delivery.repository;

import com.sparta.delivery.entity.DeliveryRecords;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRecordsJpaRepository extends JpaRepository<DeliveryRecords, UUID> {
}
