package com.sparta.gateway.jwt;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserDetail(
        Long userId,
        String username,
        String role
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return null; // 패스워드는 JWT 처리 시 일반적으로 사용되지 않음
    }

    @Override
    public String getUsername() {
        return username;
    }
}