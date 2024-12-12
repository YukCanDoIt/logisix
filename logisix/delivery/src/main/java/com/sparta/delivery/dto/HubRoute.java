package com.sparta.delivery.dto;

import java.time.Duration;
import java.util.UUID;

public record HubRoute(
        UUID hubRouteId,
        UUID departureHubId,
        UUID arrivalHubId,
        double estimatedDistance,
        Duration estimateTime
) {
}
