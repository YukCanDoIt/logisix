package com.sparta.order.domain;

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

}
