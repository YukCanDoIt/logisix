package com.sparta.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserGrantRoleRequest(
        @NotBlank String user_id,
        @NotBlank String role
) {}