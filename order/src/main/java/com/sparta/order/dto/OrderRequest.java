package com.sparta.order.dto;

import com.sparta.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor // 모든 필드를 포함한 생성자 생성
@NoArgsConstructor  // 기본 생성자 생성
public class OrderRequest {
  private UUID supplierId;
  private UUID receiverId;
  private UUID productId;
  private int quantity;
  private String requestDetails;
  private String sourceHub;
  private String destinationHub;
  private OrderStatus status;
}
