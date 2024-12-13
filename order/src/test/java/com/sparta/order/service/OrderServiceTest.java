package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderItem;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.repository.OrderRepository;
import com.sparta.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private DeliveryClient deliveryClient;

  @InjectMocks
  private OrderService orderService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("주문 생성 성공 테스트")
  void createOrder_Success() {
    UUID deliveryId = UUID.randomUUID();
    OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 10, 500);
    OrderRequest request = new OrderRequest(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        List.of(itemRequest), LocalDateTime.now(), "Test Order", "Request details"
    );

    when(deliveryClient.createDelivery(any(), eq(request))).thenReturn(deliveryId);

    Order order = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(request.supplierId())
        .receiverId(request.receiverId())
        .hubId(request.hubId())
        .orderItems(List.of(OrderItem.builder()
            .productId(itemRequest.productId())
            .quantity(itemRequest.quantity())
            .pricePerUnit(itemRequest.pricePerUnit())
            .build()))
        .status(OrderStatus.PENDING)
        .deliveryId(deliveryId)
        .createdAt(LocalDateTime.now())
        .build();

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.createOrder(request);

    assertNotNull(response);
    assertEquals(request.supplierId(), response.supplierId());
    assertEquals(deliveryId, response.deliveryId());
    assertEquals(OrderStatus.PENDING, response.status());
  }

  @Test
  @DisplayName("주문 생성 실패 - 배송 생성 오류")
  void createOrder_Fail_DeliveryCreation() {
    OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 5, 1000);
    OrderRequest request = new OrderRequest(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        List.of(itemRequest), LocalDateTime.now(), "Order Note", "Details"
    );

    when(deliveryClient.createDelivery(any(), eq(request)))
        .thenThrow(new RuntimeException("배송 생성 실패"));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(request));

    assertEquals("배송 생성 실패", exception.getMessage());
  }

  @Test
  @DisplayName("사용자 주문 조회 성공 테스트")
  void getOrdersByUser_Success() {
    UUID userId = UUID.randomUUID();
    Role role = Role.COMPANY_MANAGER;

    Order order = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(userId)
        .status(OrderStatus.PENDING)
        .orderItems(new ArrayList<>())
        .build();

    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<OrderResponse> responses = orderService.getOrdersByUser(userId, role.name());

    assertEquals(1, responses.size());
    assertEquals(userId, responses.get(0).supplierId());
  }

  @Test
  @DisplayName("주문 수정 성공 테스트")
  void updateOrder_Success() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Role role = Role.COMPANY_MANAGER;

    Order order = Order.builder()
        .id(orderId)
        .supplierId(userId)
        .orderItems(new ArrayList<>())
        .build();

    OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 5, 300);
    OrderRequest request = new OrderRequest(
        userId, UUID.randomUUID(), UUID.randomUUID(),
        List.of(itemRequest), LocalDateTime.now(), "Updated Order", "Updated details"
    );

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.updateOrder(orderId, userId, request, role.name());

    assertEquals(5, response.quantity());
    assertEquals("Updated Order", response.orderNote());
  }

  @Test
  @DisplayName("주문 삭제 성공 테스트")
  void deleteOrder_Success() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Role role = Role.MASTER;

    Order order = Order.builder()
        .id(orderId)
        .supplierId(userId)
        .isDelete(false)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    orderService.deleteOrder(orderId, userId, role.name());

    assertTrue(order.isDelete());
  }

  @Test
  @DisplayName("주문 상태 변경 성공 테스트")
  void updateOrderStatus_Success() {
    UUID orderId = UUID.randomUUID();

    Order order = Order.builder()
        .id(orderId)
        .status(OrderStatus.PENDING)
        .orderItems(new ArrayList<>())
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.updateOrderStatus(orderId, "CONFIRMED");

    assertEquals(OrderStatus.CONFIRMED, response.status());
  }
}
