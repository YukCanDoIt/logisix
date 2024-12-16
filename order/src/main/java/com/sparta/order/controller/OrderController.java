package com.sparta.order.controller;

import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.service.OrderService;
import com.sparta.order.client.UserClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;
  private final UserClient userClient;

  public OrderController(OrderService orderService, UserClient userClient) {
    this.orderService = orderService;
    this.userClient = userClient;
  }

  // 주문 생성
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(
      @RequestBody OrderRequest orderRequest,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("Authorization") String token) {

    String role = getUserRole(userId, token);
    OrderResponse orderResponse = orderService.createOrder(orderRequest, userId, role);
    return ResponseEntity.ok(orderResponse);
  }

  // 주문 검증 추가
  @PostMapping("/validate")
  public ResponseEntity<Map<String, Object>> validateOrder(@RequestBody List<OrderItemRequest> orderItems) {
    // 주문 검증 로직: orderItems가 비어있지 않고 모든 값이 유효한지 확인
    boolean isValid = orderItems != null && !orderItems.isEmpty()
        && orderItems.stream().allMatch(item -> item.productId() != null && item.quantity() > 0 && item.pricePerUnit() > 0);

    return ResponseEntity.ok(Map.of("status", isValid ? "SUCCESS" : "FAIL"));
  }

  // 사용자 주문 조회
  @GetMapping("/my")
  public ResponseEntity<List<OrderResponse>> getMyOrders(
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("Authorization") String token) {

    String role = getUserRole(userId, token);
    List<OrderResponse> orders = orderService.getOrdersByUser(userId, role);
    return ResponseEntity.ok(orders);
  }

  // 주문 수정
  @PatchMapping("/{id}")
  public ResponseEntity<OrderResponse> updateMyOrder(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("Authorization") String token,
      @RequestBody OrderRequest orderRequest) {

    String role = getUserRole(userId, token);
    OrderResponse orderResponse = orderService.updateOrder(id, userId, orderRequest, role);
    return ResponseEntity.ok(orderResponse);
  }

  // 주문 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMyOrder(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("Authorization") String token) {

    String role = getUserRole(userId, token);
    orderService.deleteOrder(id, userId, role);
    return ResponseEntity.noContent().build();
  }

  // 주문 상태 변경
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable UUID id,
      @RequestParam String status,
      @RequestHeader("X-User-ID") UUID userId,
      @RequestHeader("Authorization") String token) {

    String role = getUserRole(userId, token);
    OrderResponse response = orderService.updateOrderStatus(id, status, role);
    return ResponseEntity.ok(response);
  }

  // 사용자 역할 조회 메서드
  private String getUserRole(UUID userId, String token) {
    Map<String, String> response = userClient.getUserRole(userId, token);
    return response.get("role");
  }
}
