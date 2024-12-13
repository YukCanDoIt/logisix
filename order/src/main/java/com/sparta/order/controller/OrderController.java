package com.sparta.order.controller;

import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.service.OrderService;
import com.sparta.user.entity.Role;  // Role Enum 추가
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

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
    OrderResponse orderResponse = orderService.createOrder(orderRequest);
    return ResponseEntity.ok(orderResponse);
  }

  @GetMapping("/my")
  public ResponseEntity<List<OrderResponse>> getMyOrders(
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader(value = "X-User-Role", required = false) String role
  ) {
    List<OrderResponse> orders = orderService.getOrdersByUser(userId, role);
    return ResponseEntity.ok(orders);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<OrderResponse> updateMyOrder(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("X-User-Role") String role,
      @RequestBody OrderRequest orderRequest
  ) {
    // Role 값 변환 처리
    Role userRole = parseRole(role);

    OrderResponse orderResponse = orderService.updateOrder(id, userId, orderRequest,
        String.valueOf(userRole));
    return ResponseEntity.ok(orderResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMyOrder(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("X-User-Role") String role
  ) {
    // Role 값 변환 처리
    Role userRole = parseRole(role);

    orderService.deleteOrder(id, userId, String.valueOf(userRole));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable UUID id,
      @RequestParam String status,
      @RequestHeader("X-User-Role") String role
  ) {
    // Role 값 변환 처리
    Role userRole = parseRole(role);

    OrderResponse response = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(response);
  }

  private Role parseRole(String role) {
    try {
      return Role.valueOf(role);  // Role Enum으로 변환
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("잘못된 권한 값입니다.");  // 예외 처리
    }
  }
}
