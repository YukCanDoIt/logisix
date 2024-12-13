package com.sparta.user.controller;

import com.sparta.user.domain.User;
import com.sparta.user.dto.*;
import com.sparta.user.service.UserService;
import com.sparta.user.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ClientErrorException;
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

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            UserResponse newUser = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newUser));
        } catch (ClientErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.internalServerError());
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest request) {
        User user = userService.validateUser(request.username(), request.password());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(ApiResponse.success(token));
        } else {
            String jsonResponse = "{\"message\": \"회원 정보가 일치하지 않습니다.\"}";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonResponse);
        }
    }

    @PatchMapping("/{user_id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long user_id,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            String updatedBy = (String) httpRequest.getAttribute("username");
            UserResponse updatedUser = userService.updateUser(user_id, request, updatedBy);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.internalServerError());
            }
    }
}