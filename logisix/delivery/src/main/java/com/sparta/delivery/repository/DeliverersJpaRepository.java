package com.sparta.delivery.repository;

import com.sparta.delivery.entity.Deliverers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliverersJpaRepository extends JpaRepository<Deliverers, Long> {
    Optional<Deliverers> findByDelivererId(Long delivererId);
}
