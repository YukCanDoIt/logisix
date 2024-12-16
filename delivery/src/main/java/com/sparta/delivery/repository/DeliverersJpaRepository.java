package com.sparta.delivery.repository;

import com.sparta.delivery.entity.Deliverer;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliverersJpaRepository extends JpaRepository<Deliverer, Long> {
    Optional<Deliverer> findByDelivererId(Long delivererId);

    @Query("SELECT d FROM p_deliverers d WHERE d.hubId = :hubId AND d.type = 'COMPANY' AND d.isDeleted = false")
    List<Deliverer> findCompanyDeliverersByHub(@Param("hubId") UUID hubId);

    @Query("SELECT d FROM p_deliverers d WHERE d.type = 'HUB' AND d.isDeleted = false")
    List<Deliverer> findHubDeliverers();
}
