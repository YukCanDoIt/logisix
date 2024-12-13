package com.sparta.user.service;

import com.sparta.user.dto.UserCreateRequest;
import com.sparta.user.dto.UserResponse;
import com.sparta.user.domain.User;
import com.sparta.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 사용자 이름입니다.");
        }

        User newUser = User.create(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.slack_id(),
                request.username()
        );
        userRepository.save(newUser);
        return UserResponse.from(newUser);
    }
}