package com.sparta.user.filter;

import com.sparta.user.exception.LogisixException;
import com.sparta.user.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
        if (receivedSignature == null || !gatewaySignature.equals(receivedSignature)) {
            log.error("유효하지 않은 게이트웨이 서명입니다.");
            throw new LogisixException(ErrorCode.INVALID_SIGNATURE);
        }

        // 인증 필요 없는 경로 설정
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
                log.error("헤더에 사용자 정보가 없습니다.");
                throw new LogisixException(ErrorCode.MISSING_USER_INFORMATION);
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.error("유효하지 않은 사용자 ID입니다.", e);
                throw new LogisixException(ErrorCode.INVALID_USER_ID);
            }

            // HttpServletRequest에 사용자 정보 추가
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            log.info("HttpServletRequest에 설정된 사용자 정보 설정: userId={}, username={}, role={}", userId, username, role);

        } catch (LogisixException e) {
            throw e;
        }

        // 다음 필터 또는 서블릿으로 요청 전달
        filterChain.doFilter(request, response);
    }
}