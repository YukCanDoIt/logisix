package com.sparta.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoRouteResponse(
        Route[] routes
    ) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(
            Summary summary
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            BigDecimal distance,
            BigDecimal duration
    ) {
    }
}