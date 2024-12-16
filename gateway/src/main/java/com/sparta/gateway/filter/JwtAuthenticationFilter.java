package com.sparta.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    private final SecretKey secretKey;
    private final String gatewaySignature;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(
            @Value("${service.jwt.secret-key}") String plainSecretKey,
            @Value("${service.gateway.signature}") String gatewaySignature) {
        this.secretKey = Keys.hmacShaKeyFor(plainSecretKey.getBytes(StandardCharsets.UTF_8));
        this.gatewaySignature = gatewaySignature;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 회원가입 및 로그인 요청은 인증 제외
        if (path.startsWith("/users/sign-up") || path.startsWith("/users/sign-in")) {
            log.info("회원가입 또는 로그인 요청");
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-Gateway-Signature", gatewaySignature)
                    .build();
            ServerWebExchange updatedExchange = exchange.mutate().request(request).build();
            return chain.filter(updatedExchange);
        }

        String token = extractToken(exchange);
        if (token == null) {
            return handleUnauthorized(exchange, "인증 토큰 정보가 없습니다.");
        }

        try {
            // 토큰 검증 및 클레임 추출
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Long userId = claimsJws.getBody().get("user_id", Long.class);
            String username = claimsJws.getBody().get("username", String.class);
            String role = claimsJws.getBody().get("role", String.class);

            // 필수 클레임 유효성 확인
            if (Objects.isNull(userId) || Objects.isNull(username) || Objects.isNull(role)) {
                return handleUnauthorized(exchange, "토큰에 필수 클레임이 없습니다.");
            }

            // 사용자 정보를 요청 헤더에 추가
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .header("X-Gateway-Signature", gatewaySignature)
                    .build();

            ServerWebExchange updatedExchange = exchange.mutate().request(request).build();

            log.info("JWT 인증 성공: userId={}, username={}, role={}", userId, username, role);

            return chain.filter(updatedExchange);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("만료된 토큰: {}", e.getMessage(), e);
            return handleUnauthorized(exchange, "토큰이 만료되었습니다.");
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            log.error("토큰 검증 실패: {} - {}", e.getClass().getName(), e.getMessage());
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