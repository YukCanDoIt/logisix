package com.sparta.user.controller;

import com.sparta.user.domain.Role;
import com.sparta.user.domain.User;
import com.sparta.user.dto.request.UserCreateRequest;
import com.sparta.user.dto.request.UserGrantRoleRequest;
import com.sparta.user.dto.request.UserLoginRequest;
import com.sparta.user.dto.request.UserUpdateRequest;
import com.sparta.user.dto.response.ApiResponse;
import com.sparta.user.dto.response.PageResponse;
import com.sparta.user.dto.response.UserListResponse;
import com.sparta.user.dto.response.UserResponse;
import com.sparta.user.service.UserService;
import com.sparta.user.util.JwtUtil;
import com.sparta.user.exception.LogisixException;
import com.sparta.user.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

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

    // 단건 조회
    @GetMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserListResponse>> getUserById(
            @PathVariable("user_id") Long userId,
            HttpServletRequest httpRequest) {
        String requesterRole = (String) httpRequest.getAttribute("role");
        Long requesterId = (Long) httpRequest.getAttribute("userId");

        UserListResponse user = userService.getUserById(userId, requesterRole, requesterId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // 목록 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<UserListResponse>>> listUsers(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "slack_account", required = false) String slackAccount,
            @PageableDefault(size = 10)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "updatedAt", direction = Sort.Direction.DESC)
            })
            Pageable pageable,
            HttpServletRequest httpRequest) {

        List<Integer> allowedPageSizes = List.of(10, 30, 50);
        if (!allowedPageSizes.contains(pageable.getPageSize())) {
            throw new LogisixException(ErrorCode.INVALID_PAGE_SIZE);
        }

        String requesterRole = (String) httpRequest.getAttribute("role");
        Long requesterId = (Long) httpRequest.getAttribute("userId");

        PageResponse<UserListResponse> users = userService.listUsers(username, slackAccount, requesterRole, requesterId, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // 회원 정보 수정
    @PatchMapping("/{user_id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        String requesterRole = httpRequest.getHeader("X-User-Role");
        String updatedBy = httpRequest.getHeader("X-User-Name");

        try {
            UserResponse updatedUser = userService.updateUser(userId, request, requesterRole, updatedBy);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (LogisixException e) {
            throw e;
        } catch (Exception e) {
            throw new LogisixException(ErrorCode.API_CALL_FAILED);
        }
    }

    // 회원 정보 삭제
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable("user_id") Long userId,
            HttpServletRequest httpRequest) {
        String requesterRole = httpRequest.getHeader("X-User-Role");
        String deletedBy = httpRequest.getHeader("X-User-Name");

        try {
            userService.deleteUser(userId, requesterRole, deletedBy);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보가 삭제되었습니다."));
        } catch (LogisixException e) {
            throw e;
        } catch (Exception e) {
            throw new LogisixException(ErrorCode.API_CALL_FAILED);
        }
    }

    // 권한 부여
    @PostMapping("/grant-role")
    public ResponseEntity<?> grantRole(
            @Valid @RequestBody UserGrantRoleRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 현재 요청자의 MASTER 권한 확인
            String requesterRole = httpRequest.getHeader("X-User-Role");
            String updatedBy = httpRequest.getHeader("X-User-Name");
            if (!Role.MASTER.name().equals(requesterRole)) {
                throw new LogisixException(ErrorCode.FORBIDDEN_ACCESS);
            }

            // 권한 부여
            userService.grantRole(request, updatedBy);

            return ResponseEntity.ok(ApiResponse.success("권한이 성공적으로 부여되었습니다."));
        } catch (LogisixException e) {
            throw e;
        } catch (Exception e) {
            throw new LogisixException(ErrorCode.API_CALL_FAILED);
        }
    }

}
