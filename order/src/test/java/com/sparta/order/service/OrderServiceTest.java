package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.client.UserClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderItem;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.UnauthorizedException;
import com.sparta.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private DeliveryClient deliveryClient;

  @Mock
  private UserClient userClient; // FeignClient 사용

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
    UUID userId = UUID.randomUUID();
    OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 10, 500);

    OrderRequest request = new OrderRequest(
        userId, UUID.randomUUID(), UUID.randomUUID(),
        List.of(itemRequest), LocalDateTime.now(), "Test Order", "Details"
    );

    // FeignClient를 통해 사용자 역할 조회
    when(userClient.getUserRole(eq(userId), anyString())).thenReturn(Map.of("role", "MASTER"));
    when(deliveryClient.createDelivery(any(), eq(request))).thenReturn(deliveryId);

    Order order = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(request.supplierId())
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.createOrder(request, userId, "TOKEN");

    assertNotNull(response);
    assertEquals(OrderStatus.PENDING, response.status());
    verify(orderRepository, times(2)).save(any(Order.class));
  }

  @Test
  @DisplayName("주문 생성 실패 - 권한 오류")
  void createOrder_Fail_Unauthorized() {
    UUID userId = UUID.randomUUID();
    OrderRequest request = new OrderRequest(
        userId, UUID.randomUUID(), UUID.randomUUID(),
        List.of(), LocalDateTime.now(), "Test Order", "Details"
    );

    // FeignClient에서 역할이 MASTER가 아닌 경우
    when(userClient.getUserRole(eq(userId), anyString())).thenReturn(Map.of("role", "ANONYMOUS"));

    assertThrows(UnauthorizedException.class,
        () -> orderService.createOrder(request, userId, "TOKEN"));
  }

  @Test
  @DisplayName("사용자 주문 조회 성공 테스트")
  void getOrdersByUser_Success() {
    UUID userId = UUID.randomUUID();
    Order order = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(userId)
        .status(OrderStatus.PENDING)
        .orderItems(new ArrayList<>())
        .build();

    // 역할 조회 및 주문 반환
    when(userClient.getUserRole(eq(userId), anyString())).thenReturn(Map.of("role", "COMPANY_MANAGER"));
    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<OrderResponse> responses = orderService.getOrdersByUser(userId, "TOKEN");

    assertEquals(1, responses.size());
    verify(orderRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("주문 수정 성공 테스트")
  void updateOrder_Success() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Order order = Order.builder()
        .id(orderId)
        .supplierId(userId)
        .orderItems(new ArrayList<>())
        .build();

    OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 5, 300);
    OrderRequest request = new OrderRequest(
        userId, UUID.randomUUID(), UUID.randomUUID(),
        List.of(itemRequest), LocalDateTime.now(), "Updated Order", "Details"
    );

    when(userClient.getUserRole(eq(userId), anyString())).thenReturn(Map.of("role", "COMPANY_MANAGER"));
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.updateOrder(orderId, userId, request, "TOKEN");

    assertEquals("Updated Order", response.orderNote());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("주문 삭제 성공 테스트")
  void deleteOrder_Success() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Order order = Order.builder()
        .id(orderId)
        .supplierId(userId)
        .status(OrderStatus.PENDING)
        .build();

    when(userClient.getUserRole(eq(userId), anyString())).thenReturn(Map.of("role", "MASTER"));
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    orderService.deleteOrder(orderId, userId, "TOKEN");

    assertTrue(order.isDeleted());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("주문 상태 변경 성공 테스트")
  void updateOrderStatus_Success() {
    UUID orderId = UUID.randomUUID();

    Order order = Order.builder()
        .id(orderId)
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(userClient.getUserRole(any(), anyString())).thenReturn(Map.of("role", "MASTER"));

    OrderResponse response = orderService.updateOrderStatus(orderId, "CONFIRMED", "TOKEN");

    assertEquals(OrderStatus.CONFIRMED, response.status());
    verify(orderRepository, times(1)).save(order);
  }
}
