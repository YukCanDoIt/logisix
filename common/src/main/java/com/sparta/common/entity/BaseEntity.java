package com.sparta.common.entity;

import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

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
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    // 생성
    public void createBase(String username) {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.createdBy = username;
    }

    // 수정
    public void updateBase(String username) {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
        this.updatedBy = username;
    }

    // 삭제
    public void deleteBase(String username) {
        this.isDeleted = true;
        this.deletedAt = new Timestamp(System.currentTimeMillis());
        this.deletedBy = username;
    }
}