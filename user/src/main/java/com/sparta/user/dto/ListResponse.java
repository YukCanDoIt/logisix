package com.sparta.user.dto;

import java.util.List;

public record ListResponse<T> (
        int size,
        List<T> content
) {
    public static <T> ListResponse<T> of(List<T> content) {
        return new ListResponse<>(
                content.size(),
                content
        );
    }
}