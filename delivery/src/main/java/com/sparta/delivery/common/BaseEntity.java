package com.sparta.delivery.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    private boolean isDeleted;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private String tempUsername = "temp_username";

    // 생성
    @PrePersist
    public void createBase() {
        this.createdAt = LocalDateTime.now();
        this.createdBy = tempUsername;
    }

    // 수정
    @PreUpdate
    public void updateBase() {
        this.updatedAt =  LocalDateTime.now();
        this.updatedBy = tempUsername;
    }

    // 삭제
    public void deleteBase(String username) {
        this.isDeleted = true;
        this.deletedAt =  LocalDateTime.now();
        this.deletedBy = username;
    }
}