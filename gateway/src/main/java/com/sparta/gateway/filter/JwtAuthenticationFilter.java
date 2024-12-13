package com.sparta.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sparta.gateway.application.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    private final SecretKey secretKey;
    private final AuthService authService;
    private final String gatewaySignature;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(
            @Value("${service.jwt.secret-key}") String base64SecretKey,
            @Value("${service.gateway.signature}") String gatewaySignature,
            @Lazy AuthService authService) {
        this.secretKey = Keys.hmacShaKeyFor(base64SecretKey.getBytes(StandardCharsets.UTF_8));
        this.gatewaySignature = gatewaySignature;
        this.authService = authService;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 회원가입 및 로그인 요청은 인증 제외
        if (path.startsWith("/users/sign-up") || path.startsWith("/users/sign-in")) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);
        if (token == null) {
            return handleUnauthorized(exchange, "인증 토큰 정보가 없습니다.");
        }

        try {
            // 토큰 검증
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Long userId = claimsJws.getBody().get("user_id", Long.class);
            String username = claimsJws.getBody().get("username", String.class);
            String role = claimsJws.getBody().get("role", String.class);

            if (!authService.verifyUser(userId, username, role)) {
                return handleUnauthorized(exchange, "사용자 인증이 실패하였습니다.");
            }

            // 게이트웨이 서명 헤더 추가
            exchange.getRequest().mutate()
                    .header("X-Gateway-Signature", gatewaySignature)
                    .build();

            return chain.filter(exchange);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("만료된 토큰: {}", e.getMessage(), e);
            return handleUnauthorized(exchange, "토큰이 만료되었습니다.");
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 토큰: {}", e.getMessage(), e);
            return handleUnauthorized(exchange, "유효하지 않은 토큰입니다.");
        }
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String jsonResponse = String.format("{\"message\": \"%s\"}", message);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8)))
        );
    }
}