package com.sparta.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProductRequestDto(
    @NotBlank(message = "상품 이름을 입력해주세요.")
    @Size(max = 100, message = "상품 이름은 최대 100자까지 등록 가능합니다.")
    String productName,

    @NotBlank(message = "업체 아이디를 입력해주세요.")
    UUID companyId,

    @NotBlank(message = "수량을 입력해주세요.")
    Long quantity
) {

}
