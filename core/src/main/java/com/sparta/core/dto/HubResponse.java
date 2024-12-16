package com.sparta.core.dto;

import com.sparta.core.entity.Hub;

public record HubResponse(
    String hubName,
    String address,
    Long latitude,
    Long longitude,
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