package com.sparta.gateway.application;

public interface AuthService {
    Boolean verifyUser(Long userId, String username, String role);
}