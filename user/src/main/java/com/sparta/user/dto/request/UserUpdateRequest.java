package com.sparta.user.dto.request;

public record UserUpdateRequest(
        String password,
        String slack_account
) {}