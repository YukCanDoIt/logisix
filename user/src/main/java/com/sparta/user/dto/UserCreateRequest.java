package com.sparta.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "유저 이름을 입력해주세요.")
        @Size(min = 4, max = 10, message = "유저 이름은 최소 4자 이상, 10자 이하여야 합니다.")
        @Pattern(regexp = "^[a-z0-9]+$", message = "유저 이름은 영어 소문자와 숫자로만 구성되어야 합니다.")
        String username,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 15, message = "비밀번호는 최소 8자 이상, 15자 이하여야 합니다.")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "비밀번호는 영어 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.")
        String password,

        @NotBlank(message = "슬랙ID를 입력해주세요.")
        String slack_id
) {
}