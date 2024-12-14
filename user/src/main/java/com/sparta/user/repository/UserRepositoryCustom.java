package com.sparta.user.repository;

import com.sparta.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> findAllWithFilters(String username, String role, Boolean isDeleted, Pageable pageable);
}