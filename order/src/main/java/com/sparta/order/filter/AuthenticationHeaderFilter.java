package com.sparta.order.filter;

import com.sparta.order.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class AuthenticationHeaderFilter extends OncePerRequestFilter {

  private final String gatewaySignature;

  public AuthenticationHeaderFilter(@Value("${service.gateway.signature}") String gatewaySignature) {
    this.gatewaySignature = gatewaySignature;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String receivedSignature = request.getHeader("X-Gateway-Signature");

    // 게이트웨이 서명 확인
    if (receivedSignature == null || !gatewaySignature.equals(receivedSignature)) {
      log.error("유효하지 않은 게이트웨이 서명입니다.");
      throw new UnauthorizedException("유효하지 않은 게이트웨이 서명입니다.");
    }

    // 사용자 정보 확인
    String userIdHeader = request.getHeader("X-User-ID");
    String username = request.getHeader("X-User-Name");
    String role = request.getHeader("X-User-Role");

    if (userIdHeader == null || username == null || role == null) {
      log.error("헤더에 사용자 정보가 없습니다.");
      throw new UnauthorizedException("사용자 인증 정보가 없습니다.");
    }

    // HttpServletRequest에 사용자 정보 추가
    request.setAttribute("userId", userIdHeader);
    request.setAttribute("username", username);
    request.setAttribute("role", role);

    log.info("사용자 정보 설정: userId={}, username={}, role={}", userIdHeader, username, role);

    filterChain.doFilter(request, response);
  }
}
