package com.sparta.core.dto;

import java.util.UUID;

public record ProductResponseDto(
    String productName,
    UUID companyId,
    Long quantity
) {

}
