package com.sparta.user.controller;

import com.sparta.user.domain.Role;
import com.sparta.user.domain.User;
import com.sparta.user.dto.*;
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

@RestController
@RequestMapping("/users")
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

    // 전체 목록 조회 (MASTER 전용)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<UserListResponse>>> listUsers(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "isDeleted", required = false) Boolean isDeleted,
            @PageableDefault(size = 10)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "updatedAt", direction = Sort.Direction.DESC)
            })
            Pageable pageable,
            HttpServletRequest httpRequest) {

        try {
            // MASTER 권한 확인
            String requesterRole = httpRequest.getHeader("X-User-Role");
            if (!Role.MASTER.name().equals(requesterRole)) {
                throw new LogisixException(ErrorCode.FORBIDDEN_ACCESS);
            }

            // QueryDSL 기반 사용자 목록 조회
            PageResponse<UserListResponse> userList = userService.listUsers(username, role, isDeleted, pageable);
            return ResponseEntity.ok(ApiResponse.success(userList));
        } catch (LogisixException e) {
            throw e;
        } catch (Exception e) {
            throw new LogisixException(ErrorCode.API_CALL_FAILED);
        }
    }

    // 회원 정보 수정
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

    // 회원 정보 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(HttpServletRequest httpRequest) {
        try {
            // 헤더에서 사용자 정보 추출
            Long userId = Long.parseLong(httpRequest.getHeader("X-User-Id"));
            String deletedBy = httpRequest.getHeader("X-User-Name");

            // 사용자 삭제
            userService.deleteUser(userId, deletedBy);

            return ResponseEntity.ok(ApiResponse.success("사용자 정보가 삭제되었습니다."));
        } catch (NumberFormatException e) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
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
