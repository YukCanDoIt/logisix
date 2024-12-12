package com.sparta.core.dto;

import com.sparta.core.entity.CompanyTypeEnum;
import java.util.UUID;

public record CompanyResponseDto(
    String companyName,
    String address,
    CompanyTypeEnum companyType,
    Long latitude,
    Long longitude,
    UUID hubId,
    Long companyManagerId
) {

}
