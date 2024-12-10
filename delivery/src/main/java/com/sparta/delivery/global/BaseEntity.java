package com.sparta.delivery.global;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @Column(length = 100)
    private String deletedBy;

    @Column(nullable = false)
    private Boolean isDeleted;

    private static String tempUser = "default";

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        isDeleted = false;
        setCreatedBy(tempUser);
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        setUpdatedBy(tempUser);
    }

    public void delete() {
        deletedAt = LocalDateTime.now();
        isDeleted = true;
        deletedBy = tempUser;
    }

}
