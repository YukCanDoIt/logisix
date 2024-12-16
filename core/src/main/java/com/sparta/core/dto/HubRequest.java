package com.sparta.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record HubRequest(
    @NotBlank(message = "허브 이름을 입력해주세요.")
    @Size(max = 100, message = "허브 이름은 최대 100자까지 등록 가능합니다.")
    String hubName,

    @NotBlank(message = "허브 주소를 입력해주세요.")
    @Size(max = 100, message = "허브 주소는 최대 100자까지 등록 가능합니다.")
    String address,

    @NotNull(message = "위도를 입력해주세요.")
    BigDecimal latitude,

    @NotNull(message = "경도를 입력해주세요.")
    BigDecimal longitude,

    Long hubManagerId
) {

}