package com.sparta.user.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 게이트웨이를 통해 오는 요청인지 확인하고 사용자 정보를 처리하는 필터
 */
@Component
public class AuthenticationHeaderFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationHeaderFilter.class);

    private final String gatewaySignature; // 게이트웨이 서명 값

    public AuthenticationHeaderFilter(@Value("${service.gateway.signature}") String gatewaySignature) {
        this.gatewaySignature = gatewaySignature;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 게이트웨이 서명 확인
        String receivedSignature = request.getHeader("X-Gateway-Signature");
        log.info("도착한 게이트웨이 서명: {}", receivedSignature);
        if (!gatewaySignature.equals(receivedSignature)) {
            log.error("게이트웨이 서명이 올바르지 않거나 없습니다.");

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\": \"잘못된 접근입니다.\"}");
            return;
        }

        // 인증이 필요 없는 경로 설정
        if (path.startsWith("/users/sign-up") || path.startsWith("/users/sign-in")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 헤더에서 사용자 정보 추출
            String userIdHeader = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Name");
            String role = request.getHeader("X-User-Role");

            if (userIdHeader == null || username == null || role == null) {
                throw new IllegalArgumentException("헤더에 사용자 정보가 없습니다.");
            }

            Long userId = Long.parseLong(userIdHeader);

            // HttpServletRequest에 사용자 정보 추가
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            log.info("사용자 정보 설정: userId={}, username={}, role={}", userId, username, role);

        } catch (IllegalArgumentException e) {
            log.error("헤더에서 사용자 정보를 처리하는 데 실패했습니다.", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\": \"유효하지 않은 사용자 정보입니다.\"}");
            return;
        }

        // 다음 필터 또는 서블릿으로 요청 전달
        filterChain.doFilter(request, response);
    }
}