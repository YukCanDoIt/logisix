package com.sparta.core.entity;

public enum CompanyTypeEnum {
  PRODUCTION("Production"),
  RECEIPT("Receipt");

  private final String companyType;

  CompanyTypeEnum(String companyType) {
    this.companyType = companyType;
  }
}