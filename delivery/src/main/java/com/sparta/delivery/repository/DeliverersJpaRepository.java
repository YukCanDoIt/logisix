package com.sparta.delivery.repository;

import com.sparta.delivery.entity.Deliverer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliverersJpaRepository extends JpaRepository<Deliverer, Long> {
    Optional<Deliverer> findByDelivererId(Long delivererId);
}
