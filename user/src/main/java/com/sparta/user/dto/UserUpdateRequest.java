package com.sparta.user.dto;

public record UserUpdateRequest(
        String password,
        String slack_account
) {}