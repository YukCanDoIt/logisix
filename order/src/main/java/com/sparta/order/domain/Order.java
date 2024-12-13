package com.sparta.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "supplier_id", nullable = false)
  private UUID supplierId;

  @Column(name = "receiver_id", nullable = false)
  private UUID receiverId;

  @Column(name = "hub_id", nullable = false)
  private UUID hubId;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "order_id")
  private List<OrderItem> orderItems;

  @Column(name = "expected_delivery_date")
  private LocalDateTime expectedDeliveryDate;

  @Column(name = "order_note")
  private String orderNote;

  @Column(name = "request_details")  // 추가된 필드
  private String requestDetails;  // 요청 세부사항

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status;

  @Column(name = "is_delivery_started", nullable = false)
  @Builder.Default
  private boolean isDeliveryStarted = false; // 배송 시작 여부

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Setter
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Setter
  @Column(name = "delivery_id", nullable = true)
  private UUID deliveryId;

  @Column(name = "is_delete", nullable = false)
  @Builder.Default
  private boolean isDelete = false;

  public void markAsDeleted() {
    this.isDelete = true;
    this.updatedAt = LocalDateTime.now();
  }

  public void setStatus(OrderStatus status) {
    if (isDeliveryStarted) {
      throw new IllegalStateException("배송이 시작된 이후에는 상태를 변경할 수 없습니다.");
    }
    this.status = status;
    this.updatedAt = LocalDateTime.now();
  }

  public void startDelivery() {
    this.isDeliveryStarted = true;
  }

  public void updateOrder(List<OrderItem> updatedItems, String orderNote, LocalDateTime expectedDeliveryDate) {
    this.orderItems.clear();
    this.orderItems.addAll(updatedItems);

    if (orderNote != null) {
      this.orderNote = orderNote;
    }

    if (expectedDeliveryDate != null) {
      this.expectedDeliveryDate = expectedDeliveryDate;
    }

    this.updatedAt = LocalDateTime.now();
  }

  // isDeliveryStarted
  public boolean getIsDeliveryStarted() {
    return this.isDeliveryStarted;
  }

  // requestDetails와 orderNote
  public String getOrderNote() {
    return this.orderNote;
  }

  public String getRequestDetails() {
    return this.requestDetails;
  }
}
