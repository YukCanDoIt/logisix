package com.sparta.user.dto;

public record UserUpdateRequest(
        String password,
        String slackAccount
) {}