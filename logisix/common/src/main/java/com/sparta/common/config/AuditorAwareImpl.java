package com.sparta.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (null == authentication || !authentication.isAuthenticated()) {
            return null;
        }

        //사용자 환경에 맞게 로그인한 사용자의 정보를 불러온다.
        log.info("authentication.getName(): {}", authentication.getName());
        if (authentication.getName().equals("anonymousUser")) {
            return Optional.of("anonymous");
        }

        return Optional.of(authentication.getName());
    }
}