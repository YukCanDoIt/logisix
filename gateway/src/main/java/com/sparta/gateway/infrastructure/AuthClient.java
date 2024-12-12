package com.sparta.gateway.infrastructure;

import com.sparta.gateway.application.AuthService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthClient extends AuthService {
    @GetMapping("/auth/verify")
    Boolean verifyUser(@RequestParam(value = "user_id") String userId);
}