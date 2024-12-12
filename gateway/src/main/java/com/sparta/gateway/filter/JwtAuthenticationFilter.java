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

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(
            @Value("${service.jwt.secret-key}") String base64SecretKey,
            @Lazy AuthService authService) {
        this.secretKey = Keys.hmacShaKeyFor(base64SecretKey.getBytes(StandardCharsets.UTF_8));
        this.authService = authService;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/users/signUp") || path.startsWith("/users/signIn")) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);
        if (token == null || !validateToken(token)) {
            return handleUnauthorized(exchange);
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Long userId = claimsJws.getBody().get("user_id", Long.class);
            String username = claimsJws.getBody().get("username", String.class);
            String role = claimsJws.getBody().get("role", String.class);

            return authService.verifyUser(userId, username, role);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("만료된 토큰: {}", e.getMessage(), e);
            return false;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            log.error("적절하지 않은 토큰: {}", e.getMessage(), e);
            return false;
        }
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String jsonResponse = "{\"message\": \"인가되지 않은 접근입니다.\"}";

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8)))
        );
    }
}