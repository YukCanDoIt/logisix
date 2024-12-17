package com.sparta.order.domain;

import com.sparta.order.dto.OrderRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id; // 주문 고유 식별자는 UUID 유지

  @Column(name = "supplier_id", nullable = false)
  private long supplierId; // long 타입

  @Column(name = "receiver_id", nullable = false)
  private long receiverId; // long 타입

  @Column(name = "hub_id", nullable = false)
  private UUID hubId; // UUID 타입 유지

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "order_id")
  @Builder.Default
  private List<OrderItem> orderItems = new ArrayList<>();

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

  // 정적 팩토리 메서드
  public static Order create(OrderRequest orderRequest) {
    return Order.builder()
        .supplierId(orderRequest.supplierId())
        .receiverId(orderRequest.receiverId())
        .hubId(orderRequest.hubId())
        .orderItems(orderRequest.orderItems().stream()
            .map(item -> new OrderItem(item.productId(), item.quantity(), item.pricePerUnit()))
            .collect(Collectors.toList()))
        .expectedDeliveryDate(orderRequest.expectedDeliveryDate())
        .orderNote(orderRequest.orderNote())
        .requestDetails(orderRequest.requestDetails()) // requestDetails 설정 추가
        .status(OrderStatus.PENDING)
        .build();
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
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

    this.setUpdatedAt(LocalDateTime.now());
  }

  // 테스트 용도 setter 추가 (패키지-프라이빗)
  public void setId(UUID id) {
    this.id = id;
  }

  public void markAsDeleted() {
    this.isDeleted = true;
  }

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted;
}
