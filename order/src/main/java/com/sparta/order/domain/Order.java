package com.sparta.order.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

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
  @Builder.Default
  private List<OrderItem> orderItems = new ArrayList<>(); // NullPointerException 방지

  @Column(name = "expected_delivery_date")
  private LocalDateTime expectedDeliveryDate;

  @Column(name = "order_note")
  private String orderNote;

  @Column(name = "request_details")
  private String requestDetails;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status;

  @Setter
  @Column(name = "delivery_id")
  private UUID deliveryId;

  // 주문 상태 변경 메서드
  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  // 주문 정보 업데이트
  public void updateOrder(List<OrderItem> updatedItems, String orderNote, LocalDateTime expectedDeliveryDate) {
    this.orderItems.clear();
    this.orderItems.addAll(updatedItems);

    if (orderNote != null) {
      this.orderNote = orderNote;
    }

    if (expectedDeliveryDate != null) {
      this.expectedDeliveryDate = expectedDeliveryDate;
    }

    this.setUpdatedAt(LocalDateTime.now());
  }
}
