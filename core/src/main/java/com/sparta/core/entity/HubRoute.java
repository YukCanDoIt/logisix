package com.sparta.core.entity;

import com.sparta.core.dto.HubRouteRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_hub_route")
@Getter
@NoArgsConstructor
public class HubRoute extends Base {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID hubRouteId;

  @Column(name = "arrival_hub_id", nullable = false)
  private UUID arrivalHubId;

  @Column(name = "departure_hub_id", nullable = false)
  private UUID departureHubId;

  @Column(name = "estimated_distance", nullable = false)
  private double estimatedDistance;

  @Column(name = "estimated_time", nullable = false)
  private double estimatedTime;

  public HubRoute(HubRouteRequest hubRouteRequest) {
    this.arrivalHubId = hubRouteRequest.arrivalHubId();
    this.departureHubId = hubRouteRequest.departureHubId();
    this.estimatedDistance = hubRouteRequest.estimatedDistance();
    this.estimatedTime = hubRouteRequest.estimatedTime();
  }

}
