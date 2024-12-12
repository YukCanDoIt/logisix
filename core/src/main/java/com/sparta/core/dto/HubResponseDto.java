package com.sparta.core.dto;

public record HubResponseDto(
    String hubName,
    String address,
    Long latitude,
    Long longitude,
    Long hubManagerId
) {

}