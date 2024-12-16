package com.sparta.core.repository;

import com.sparta.core.entity.HubRoute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HubRouteRepository extends JpaRepository<HubRoute, UUID> {

  HubRoute findByArrivalHubIdAndDepartureHubId(UUID arrivalHubId, UUID durationHubId);
}
