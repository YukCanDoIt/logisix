package com.sparta.user.controller;

import com.sparta.user.domain.User;
import com.sparta.user.dto.*;
import com.sparta.user.service.UserService;
import com.sparta.user.util.JwtUtil;
import com.sparta.user.exception.LogisixException;
import com.sparta.user.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // 회원가입
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            UserResponse newUser = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newUser));
        } catch (IllegalArgumentException e) {
            throw new LogisixException(ErrorCode.DUPLICATE_USERNAME);
        } catch (Exception e) {
            throw new LogisixException(ErrorCode.API_CALL_FAILED);
        }
    }

    // 로그인
    @PostMapping("/sign-in")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest request) {
        try {
            User user = userService.validateUser(request.username(), request.password());
            if (user != null) {
                String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
                return ResponseEntity.ok(ApiResponse.success(token));
            } else {
                throw new LogisixException(ErrorCode.INVALID_PASSWORD);
            }
        } catch (LogisixException e) {
            throw e;
        } catch (Exception e) {
            throw new LogisixException(ErrorCode.API_CALL_FAILED);
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 헤더에서 사용자 정보 추출
            Long userId = Long.parseLong(httpRequest.getHeader("X-User-Id"));
            String updatedBy = httpRequest.getHeader("X-User-Name");

            // 사용자 정보 업데이트
            UserResponse updatedUser = userService.updateUser(userId, request, updatedBy);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (NumberFormatException e) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
        } catch (LogisixException e) {
            throw e;
        } catch (Exception e) {
            throw new LogisixException(ErrorCode.API_CALL_FAILED);
        }
    }
}
