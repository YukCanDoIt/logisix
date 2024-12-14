package com.sparta.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.user.domain.QUser;
import com.sparta.user.domain.User;
import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<User> findAllWithFilters(String username, String role, Boolean isDeleted, Pageable pageable) {
        QUser user = QUser.user;

        BooleanBuilder whereClause = new BooleanBuilder();

        if (username != null && !username.isEmpty()) {
            whereClause.and(user.username.containsIgnoreCase(username));
        }
        if (role != null && !role.isEmpty()) {
            whereClause.and(user.role.eq(com.sparta.user.domain.Role.valueOf(role)));
        }
        if (isDeleted != null) {
            whereClause.and(user.isDeleted.eq(isDeleted));
        }

        List<User> users = queryFactory
                .selectFrom(user)
                .where(whereClause)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .where(whereClause)
                .fetchCount();

        return PageableExecutionUtils.getPage(users, pageable, () -> total);
    }
}