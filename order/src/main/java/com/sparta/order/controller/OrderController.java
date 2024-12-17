package com.sparta.order.controller;

import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  // 주문 생성
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(
      @RequestBody OrderRequest orderRequest,
      HttpServletRequest request) {

    long userId = Long.parseLong(request.getHeader("X-User-ID"));
    String role = request.getHeader("X-User-Role");
    OrderResponse orderResponse = orderService.createOrder(orderRequest, userId, role);
    return ResponseEntity.ok(orderResponse);
  }

  // 주문 검증 추가
  @PostMapping("/validate")
  public ResponseEntity<Map<String, Object>> validateOrder(@RequestBody List<OrderItemRequest> orderItems) {
    boolean isValid = orderItems != null && !orderItems.isEmpty()
        && orderItems.stream().allMatch(item -> item.productId() != null && item.quantity() > 0 && item.pricePerUnit() > 0);

    return ResponseEntity.ok(Map.of("status", isValid ? "SUCCESS" : "FAIL"));
  }

  // 사용자 주문 조회
  @GetMapping("/my")
  public ResponseEntity<List<OrderResponse>> getMyOrders(HttpServletRequest request) {
    long userId = Long.parseLong(request.getHeader("X-User-ID"));
    String role = request.getHeader("X-User-Role");
    List<OrderResponse> orders = orderService.getOrdersByUser(userId, role);
    return ResponseEntity.ok(orders);
  }

  // 주문 수정
  @PatchMapping("/{id}")
  public ResponseEntity<OrderResponse> updateMyOrder(
      @PathVariable UUID id,
      @RequestBody OrderRequest orderRequest,
      HttpServletRequest request) {

    long userId = Long.parseLong(request.getHeader("X-User-ID"));
    String role = request.getHeader("X-User-Role");
    OrderResponse orderResponse = orderService.updateOrder(id, userId, orderRequest, role);
    return ResponseEntity.ok(orderResponse);
  }

  // 주문 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMyOrder(@PathVariable UUID id, HttpServletRequest request) {
    long userId = Long.parseLong(request.getHeader("X-User-ID"));
    String role = request.getHeader("X-User-Role");
    orderService.deleteOrder(id, userId, role);
    return ResponseEntity.noContent().build();
  }

  // 주문 상태 변경
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable UUID id, // UUID로 변경
      @RequestParam String status,
      HttpServletRequest request) {

    String role = request.getHeader("X-User-Role");
    OrderResponse response = orderService.updateOrderStatus(id, status, role);
    return ResponseEntity.ok(response);
  }
}
