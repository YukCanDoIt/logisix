package com.sparta.delivery.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateDeliveryRequest(
        @NotNull UUID orderId,
        @NotNull UUID sourceHubId,
        @NotNull UUID destinationId,
        @NotNull String companyAddress,
        @NotNull String recipient,
        @NotNull String recipientSlackAccount,
        @NotNull UUID companyId,

        @NotNull LocalDateTime deliverDate
) { }