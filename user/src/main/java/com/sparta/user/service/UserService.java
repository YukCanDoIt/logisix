package com.sparta.user.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.user.domain.QUser;
import com.sparta.user.domain.User;
import com.sparta.user.domain.Role;
import com.sparta.user.dto.request.UserCreateRequest;
import com.sparta.user.dto.request.UserGrantRoleRequest;
import com.sparta.user.dto.request.UserUpdateRequest;
import com.sparta.user.dto.response.PageResponse;
import com.sparta.user.dto.response.UserListResponse;
import com.sparta.user.dto.response.UserResponse;
import com.sparta.user.exception.LogisixException;
import com.sparta.user.exception.ErrorCode;
import com.sparta.user.repository.UserRepository;
import com.sparta.user.util.QueryDslUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sparta.user.util.QueryDslUtil.getOrderSpecifiers;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JPAQueryFactory queryFactory;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JPAQueryFactory queryFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.queryFactory = queryFactory;
    }

    // 회원가입
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new LogisixException(ErrorCode.DUPLICATE_USERNAME);
        }

        QUser qUser = QUser.user;
        BooleanExpression condition = qUser.username.eq(request.username())
                .and(isNotDeleted(qUser));

        User existingUser = queryFactory
                .selectFrom(qUser)
                .where(condition)
                .fetchOne();

        if (existingUser != null) {
            throw new LogisixException(ErrorCode.DUPLICATE_USERNAME);
        }

        User newUser = User.create(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.slack_account(),
                request.username()
        );
        userRepository.save(newUser);
        return UserResponse.from(newUser);
    }

    // 로그인용 유저 검증
    public User validateUser(String username, String password) {
        QUser qUser = QUser.user;

        BooleanExpression condition = qUser.username.eq(username).and(isNotDeleted(qUser));

        User user = queryFactory
                .selectFrom(qUser)
                .where(condition)
                .fetchOne();

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new LogisixException(ErrorCode.INVALID_PASSWORD);
        }
        return user;
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public UserListResponse getUserById(Long userId, String requesterRole, Long requesterId) throws LogisixException {
        QUser qUser = QUser.user;

        BooleanExpression condition = qUser.userId.eq(userId).and(isNotDeleted(qUser));

        if (!Role.MASTER.name().equals(requesterRole) && !userId.equals(requesterId)) {
            throw new LogisixException(ErrorCode.FORBIDDEN_ACCESS);
        }

        User user = queryFactory
                .selectFrom(qUser)
                .where(condition)
                .fetchOne();

        if (user == null) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
        }

        return UserListResponse.from(user);
    }

    // 목록 조회
    @Transactional(readOnly = true)
    public PageResponse<UserListResponse> listUsers(String username, String slackAccount, String requesterRole, Long requesterId, Pageable pageable) throws LogisixException {
        QUser qUser = QUser.user;

        BooleanExpression condition = isNotDeleted(qUser);

        if (username != null && !username.isBlank()) {
            condition = condition.and(qUser.username.containsIgnoreCase(username));
        }
        if (slackAccount != null && !slackAccount.isBlank()) {
            condition = condition.and(qUser.slackAccount.containsIgnoreCase(slackAccount));
        }

        if (!Role.MASTER.name().equals(requesterRole)) {
            condition = condition.and(qUser.userId.eq(requesterId));
        }

        Map<String, ComparableExpressionBase<?>> fieldMap = new HashMap<>();
        fieldMap.put("createdAt", qUser.createdAt);
        fieldMap.put("updatedAt", qUser.updatedAt);

        List<User> users = queryFactory
                .selectFrom(qUser)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QueryDslUtil.getOrderSpecifiers(pageable.getSort(), fieldMap))
                .fetch();

        long total = queryFactory
                .select(qUser.count())
                .from(qUser)
                .where(condition)
                .fetchOne();

        if (total == 0) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
        }

        return new PageResponse<>(
                pageable.getPageNumber() + 1,
                (int) Math.ceil((double) total / pageable.getPageSize()),
                users.stream().map(UserListResponse::from).toList()
        );
    }

    // 회원 정보 수정
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request, String requesterRole, String updatedBy) {
        if (!Role.MASTER.name().equals(requesterRole)) {
            throw new LogisixException(ErrorCode.FORBIDDEN_ACCESS);
        }

        QUser qUser = QUser.user;
        BooleanExpression condition = qUser.userId.eq(userId).and(isNotDeleted(qUser));

        User user = queryFactory
                .selectFrom(qUser)
                .where(condition)
                .fetchOne();

        if (user == null) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
        }

        String encodedPassword = request.password() != null
                ? passwordEncoder.encode(request.password())
                : null;

        user.update(encodedPassword, request.slack_account(), updatedBy);
        user.updateBase(updatedBy);
        return UserResponse.from(user);
    }

    // 회원 삭제
    @Transactional
    public void deleteUser(Long userId, String requesterRole, String deletedBy) {
        if (!Role.MASTER.name().equals(requesterRole)) {
            throw new LogisixException(ErrorCode.FORBIDDEN_ACCESS);
        }

        QUser qUser = QUser.user;
        BooleanExpression condition = qUser.userId.eq(userId).and(isNotDeleted(qUser));

        User user = queryFactory
                .selectFrom(qUser)
                .where(condition)
                .fetchOne();

        if (user == null) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
        }

        user.deleteBase(deletedBy);
        userRepository.save(user);
    }

    // 회원 권한 부여 (MASTER 전용)
    @Transactional
    public void grantRole(UserGrantRoleRequest request, String updatedBy) {
        long userId;
        try {
            userId = Long.parseLong(request.user_id());
        } catch (NumberFormatException e) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
        }

        QUser qUser = QUser.user;
        BooleanExpression condition = qUser.userId.eq(userId).and(isNotDeleted(qUser));

        // 삭제되지 않은 사용자 확인
        User user = queryFactory
                .selectFrom(qUser)
                .where(condition)
                .fetchOne();

        if (user == null) {
            throw new LogisixException(ErrorCode.USER_NOT_FOUND);
        }

        Role newRole;
        try {
            newRole = Role.valueOf(request.role());
        } catch (IllegalArgumentException e) {
            throw new LogisixException(ErrorCode.INVALID_REQUEST_DATA);
        }

        user.grantRole(newRole, updatedBy);
        user.updateBase(updatedBy);

        userRepository.save(user);
    }

    private BooleanExpression isNotDeleted(QUser qUser) {
        return qUser.isDeleted.isFalse();
    }
}