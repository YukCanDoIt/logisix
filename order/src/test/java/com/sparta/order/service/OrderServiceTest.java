package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.LogisixException;
import com.sparta.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;
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
    long userId = 12345L;
    long supplierId = 100L;
    long receiverId = 200L;
    UUID hubId = UUID.randomUUID();

    OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 10, 500);
    OrderRequest request = new OrderRequest(
        supplierId, receiverId, hubId,
        List.of(itemRequest), LocalDateTime.now(), "Test Order", "Details"
    );

    when(deliveryClient.createDelivery(any(), eq(request))).thenReturn(deliveryId);

    Order order = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(supplierId)
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.createOrder(request, userId, "MASTER");

    assertNotNull(response);
    assertEquals(OrderStatus.PENDING, response.status());
    verify(orderRepository, times(2)).save(any(Order.class));
  }

  @Test
  @DisplayName("주문 생성 실패 - 권한 오류")
  void createOrder_Fail_Unauthorized() {
    long userId = 12345L;
    long supplierId = 100L;
    long receiverId = 200L;
    UUID hubId = UUID.randomUUID();

    OrderRequest request = new OrderRequest(
        supplierId, receiverId, hubId,
        List.of(new OrderItemRequest(UUID.randomUUID(), 1, 100)),
        LocalDateTime.now(), "Test Order", "Details"
    );

    assertThrows(LogisixException.class,
        () -> orderService.createOrder(request, userId, "ANONYMOUS"));
  }

  @Test
  @DisplayName("사용자 주문 조회 성공 테스트")
  void getOrdersByUser_Success() {
    long userId = 12345L;
    long supplierId = userId; // userId를 supplierId로 사용
    Order order = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(supplierId)
        .status(OrderStatus.PENDING)
        .orderItems(new ArrayList<>())
        .build();

    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<OrderResponse> responses = orderService.getOrdersByUser(userId, "COMPANY_MANAGER");

    assertEquals(1, responses.size());
    verify(orderRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("주문 수정 성공 테스트")
  void updateOrder_Success() {
    UUID orderId = UUID.randomUUID();
    long userId = 12345L;
    long supplierId = userId;
    long receiverId = 200L;
    UUID hubId = UUID.randomUUID();

    Order order = Order.builder()
        .id(orderId)
        .supplierId(supplierId)
        .orderItems(new ArrayList<>())
        .build();

    OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), 5, 300);
    OrderRequest request = new OrderRequest(
        supplierId, receiverId, hubId,
        List.of(itemRequest), LocalDateTime.now(), "Updated Order", "Details"
    );

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.updateOrder(orderId, userId, request, "COMPANY_MANAGER");

    assertEquals("Updated Order", response.orderNote());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("주문 삭제 성공 테스트")
  void deleteOrder_Success() {
    UUID orderId = UUID.randomUUID();
    long userId = 12345L;
    long supplierId = userId;

    Order order = Order.builder()
        .id(orderId)
        .supplierId(supplierId)
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    orderService.deleteOrder(orderId, userId, "MASTER");

    assertTrue(order.isDeleted());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("주문 상태 변경 성공 테스트")
  void updateOrderStatus_Success() {
    UUID orderId = UUID.randomUUID();
    long userId = 12345L; // userId 선언했지만 사용하지 않는다면 제거 가능

    Order order = Order.builder()
        .id(orderId)
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.updateOrderStatus(orderId, "CONFIRMED", "MASTER");

    assertEquals(OrderStatus.CONFIRMED, response.status());
    verify(orderRepository, times(1)).save(order);
  }
}
