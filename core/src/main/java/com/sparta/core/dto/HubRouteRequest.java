package com.sparta.core.dto;

import com.sparta.core.entity.HubRoute;
import java.util.UUID;

public record HubRouteRequest(
    UUID arrivalHubId,
    UUID departureHubId,
    double estimatedDistance,
    double estimatedTime
) {

  public static HubRouteRequest from(HubRoute hubRoute) {
    return new HubRouteRequest(
        hubRoute.getArrivalHubId(),
        hubRoute.getDepartureHubId(),
        hubRoute.getEstimatedDistance(),
        hubRoute.getEstimatedTime()
    );
  }
}
