package com.sparta.core.dto;

import com.sparta.core.entity.Hub;
import com.sparta.core.entity.HubRoute;
import java.util.UUID;

public record HubRouteResponse(
    UUID arrivalHubId,
    UUID departureHubId,
    double estimatedDistance,
    double estimatedTime
) {

  public static HubRouteResponse from(HubRoute hubRoute) {
    return new HubRouteResponse(
        hubRoute.getArrivalHubId(),
        hubRoute.getDepartureHubId(),
        hubRoute.getEstimatedDistance(),
        hubRoute.getEstimatedTime()
    );
  }
}
