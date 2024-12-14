package com.sparta.user.dto.response;

import com.sparta.user.domain.User;

import java.time.LocalDateTime;

public record UserListResponse(
        Long userId,
        String username,
        String slackId,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserListResponse from(User user) {
        return new UserListResponse(
                user.getUserId(),
                user.getUsername(),
                user.getSlackAccount(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}