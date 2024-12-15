package com.sparta.delivery.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeDelivererRequest(
        @NotNull Long delivererId
)  { }
