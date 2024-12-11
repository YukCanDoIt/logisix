package com.sparta.order.controller;

import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.UnauthorizedException;
import com.sparta.order.service.OrderService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  public ResponseEntity<List<OrderResponse>> getMyOrders(
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader(value = "X-User-Role", required = false) String role // 역할 정보 추가
  ) {
    List<OrderResponse> orders = orderService.getOrdersByUser(userId, role); // 역할 전달
    return ResponseEntity.ok(orders);
  }

  // 본인 주문 수정
  @PatchMapping("/{id}")
  public ResponseEntity<OrderResponse> updateMyOrder(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("X-User-Role") String role, // 역할 정보 추가
      @RequestBody OrderRequest orderRequest
  ) {
    OrderResponse orderResponse = orderService.updateOrder(id, userId, orderRequest, role); // 역할 전달
    return ResponseEntity.ok(orderResponse);
  }

  // 본인 주문 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMyOrder(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("X-User-Role") String role // 역할 정보 추가
  ) {
    orderService.deleteOrder(id, userId, role); // 역할 전달
    return ResponseEntity.noContent().build();
  }

  // 주문 상태 변경
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable UUID id,
      @RequestParam String status,
      @RequestHeader("X-User-Role") String role // 역할 정보 추가
  ) {
    // 역할 확인 로직 추가
    if (!"MASTER_ADMIN".equals(role)) {
      throw new UnauthorizedException("상태 변경 권한이 없습니다.");
    }
    OrderResponse response = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(response);
  }
}
