package com.sparta.delivery.dto;

import java.math.BigDecimal;

public record UpdateDeliveryStatusRequest(
        BigDecimal actualDist
) {
}
