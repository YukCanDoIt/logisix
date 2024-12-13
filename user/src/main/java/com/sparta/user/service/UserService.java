package com.sparta.user.service;

import com.sparta.user.dto.UserCreateRequest;
import com.sparta.user.dto.UserResponse;
import com.sparta.user.domain.User;
import com.sparta.user.dto.UserUpdateRequest;
import com.sparta.user.exception.LogisixException;
import com.sparta.user.exception.ErrorCode;
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

    // 회원가입
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new LogisixException(ErrorCode.DUPLICATE_USERNAME);
        }

        User existingUser = userRepository.findByUsername(request.username());
        if (existingUser != null) {
            throw new LogisixException(ErrorCode.DUPLICATE_USERNAME);
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

    // 로그인용 유저 검증
    public User validateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new LogisixException(ErrorCode.INVALID_PASSWORD);
        }
        return user;
    }

    // 회원 정보 수정
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request, String updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LogisixException(ErrorCode.USER_NOT_FOUND));

        // 수정된 패스워드 암호화
        String encodedPassword = request.password() != null
                ? passwordEncoder.encode(request.password())
                : null;

        user.update(encodedPassword, request.slackAccount(), updatedBy);
        user.updateBase(updatedBy);
        return UserResponse.from(user);
    }
}