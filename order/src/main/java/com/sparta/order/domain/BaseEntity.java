package com.sparta.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "is_delivery_started", nullable = false)
  private boolean isDeliveryStarted = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // 명시적으로 isDeleted 메서드 추가
  public boolean isDeleted() {
    return this.isDeleted;
  }

  public void markAsDeleted() {
    this.isDeleted = true;
    this.updatedAt = LocalDateTime.now();
  }

  public void startDelivery() {
    this.isDeliveryStarted = true;
  }
}
