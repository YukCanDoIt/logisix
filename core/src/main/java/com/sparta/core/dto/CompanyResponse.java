package com.sparta.core.dto;

import com.sparta.core.entity.Company;
import com.sparta.core.entity.CompanyTypeEnum;
import java.util.UUID;

public record CompanyResponse(
    String companyName,
    String address,
    CompanyTypeEnum companyType,
    Long latitude,
    Long longitude,
    UUID hubId,
    Long companyManagerId
) {

  public static CompanyResponse from(Company company) {
    return new CompanyResponse(
        company.getCompanyName(),
        company.getAddress(),
        company.getType(),
        company.getLatitude(),
        company.getLongitude(),
        company.getHubId(),
        company.getCompanyManagerId()
    );
  }
}
