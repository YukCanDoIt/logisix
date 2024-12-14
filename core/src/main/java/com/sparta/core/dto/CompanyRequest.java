package com.sparta.core.dto;

import com.sparta.core.entity.CompanyTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CompanyRequest(
    @NotBlank(message = "업체 이름을 입력해주세요.")
    @Size(max = 100, message = "업체 이름은 최대 100자까지 등록 가능합니다.")
    String companyName,

    @NotBlank(message = "업체 주소를 입력해주세요.")
    @Size(max = 100, message = "업체 주소는 최대 100자까지 등록 가능합니다.")
    String address,

    @Enumerated
    CompanyTypeEnum companyType,

    @NotNull(message = "위도를 입력해주세요.")
    BigDecimal latitude,

    @NotNull(message = "경도를 입력해주세요.")
    BigDecimal longitude,

    @NotNull(message = "허브 아이디를 입력해주세요.")
    UUID hubId
) {

}
