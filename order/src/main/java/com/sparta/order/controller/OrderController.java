package com.sparta.order.controller;

import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  // 주문 생성
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
    OrderResponse orderResponse = orderService.createOrder(orderRequest);
    return ResponseEntity.ok(orderResponse);
  }

  // 본인 주문 조회
  @GetMapping("/my")
  public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader("X-User-ID") UUID userId) {
    List<OrderResponse> orders = orderService.getOrdersByUser(userId);
    return ResponseEntity.ok(orders);
  }

  // 본인 주문 수정
  @PatchMapping("/{id}")
  public ResponseEntity<OrderResponse> updateMyOrder(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestBody OrderRequest orderRequest) {
    OrderResponse orderResponse = orderService.updateOrder(id, userId, orderRequest);
    return ResponseEntity.ok(orderResponse);
  }

  // 본인 주문 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMyOrder(@PathVariable UUID id, @RequestHeader("X-User-ID") UUID userId) {
    orderService.deleteOrder(id, userId);
    return ResponseEntity.noContent().build();
  }

  // 주문 상태 변경
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable UUID id,
      @RequestParam String status // 쿼리 파라미터로 변경
  ) {
    OrderResponse response = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(response);
  }
}
