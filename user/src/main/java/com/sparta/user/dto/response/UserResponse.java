package com.sparta.user.dto.response;

import com.sparta.user.domain.User;

public record UserResponse (
        Long user_id,
        String username
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername()
        );
    }
}