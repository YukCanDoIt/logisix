package com.sparta.delivery.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDeliveryRequest(
        @NotNull UUID orderId,
        @NotNull UUID sourceHubId,
        @NotNull UUID destinationId
        ) { }
