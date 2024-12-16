package com.sparta.order.domain;

import com.sparta.order.dto.OrderItemRequest; // OrderItemRequest DTO import
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Setter
  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "price_per_unit", nullable = false)
  private int pricePerUnit;

  public OrderItem(UUID uuid, int i, int i1) {
  }

  // OrderItem -> OrderItemRequest 변환 메서드
  public OrderItemRequest toRequest() {
    return new OrderItemRequest(this.productId, this.quantity, this.pricePerUnit);
  }
}
