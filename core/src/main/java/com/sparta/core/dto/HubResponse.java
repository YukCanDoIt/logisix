package com.sparta.core.dto;

import com.sparta.core.entity.Hub;
import java.math.BigDecimal;

public record HubResponse(
    String hubName,
    String address,
    BigDecimal latitude,
    BigDecimal longitude,
    Long hubManagerId
) {

  public static HubResponse from(Hub hub) {
    return new HubResponse(
        hub.getHubName(),
        hub.getAddress(),
        hub.getLatitude(),
        hub.getLongitude(),
        hub.getHubManagerId()
    );
  }

}