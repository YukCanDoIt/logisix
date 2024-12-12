package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;

import com.sparta.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
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
    OrderRequest request = new OrderRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        10,
        "Urgent delivery",
        "Source Hub",
        "Destination Hub",
        null
    );

    UUID deliveryId = UUID.randomUUID();
    when(deliveryClient.createDelivery(any(), eq(request))).thenReturn(deliveryId);

    Order savedOrder = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(request.getSupplierId())
        .receiverId(request.getReceiverId())
        .productId(request.getProductId())
        .quantity(request.getQuantity())
        .deliveryId(deliveryId)
        .status(OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

    OrderResponse response = orderService.createOrder(request);

    assertNotNull(response);
    assertEquals(request.getSupplierId(), response.supplierId());
    assertEquals(deliveryId, response.deliveryId());
    assertEquals(OrderStatus.PENDING, response.status());
  }

  @Test
  @DisplayName("사용자 주문 조회 성공")
  void getOrdersByUser_Success() {
    UUID userId = UUID.randomUUID();
    String role = "SUPPLIER_USER";

    Order order = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(userId)
        .receiverId(UUID.randomUUID())
        .productId(UUID.randomUUID())
        .quantity(10)
        .requestDetails("Test order")
        .status(OrderStatus.PENDING)
        .isDelete(false)
        .createdAt(LocalDateTime.now())
        .build();

    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<OrderResponse> responses = orderService.getOrdersByUser(userId, role);

    assertEquals(1, responses.size());
    assertEquals(userId, responses.get(0).supplierId());
  }

  @Test
  @DisplayName("주문 삭제 성공 - 논리 삭제 처리")
  void deleteOrder_LogicalDelete() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "MASTER_ADMIN";

    Order order = Order.builder()
        .id(orderId)
        .supplierId(userId)
        .isDelete(false)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    orderService.deleteOrder(orderId, userId, role);

    assertTrue(order.isDelete());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("주문 수정 성공")
  void updateOrder_Success() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "SUPPLIER_USER";

    Order order = Order.builder()
        .id(orderId)
        .supplierId(userId)
        .quantity(5)
        .build();

    OrderRequest request = new OrderRequest(
        userId,
        UUID.randomUUID(),
        UUID.randomUUID(),
        10,
        "Updated request details",
        "Source Hub",
        "Destination Hub",
        null
    );

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.updateOrder(orderId, userId, request, role);

    assertEquals(10, response.quantity());
    assertEquals("Updated request details", response.requestDetails());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("주문 상태 변경 성공")
  void updateOrderStatus_Success() {
    UUID orderId = UUID.randomUUID();
    String newStatus = "CONFIRMED";

    Order order = Order.builder()
        .id(orderId)
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.updateOrderStatus(orderId, newStatus);

    assertEquals(OrderStatus.CONFIRMED, response.status());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("주문 상태 변경 실패 - 유효하지 않은 상태 전환")
  void updateOrderStatus_InvalidTransition() {
    UUID orderId = UUID.randomUUID();
    Order order = Order.builder()
        .id(orderId)
        .status(OrderStatus.CONFIRMED)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      orderService.updateOrderStatus(orderId, "PENDING");
    });

    assertEquals("CONFIRMED 상태에서 PENDING으로 변경할 수 없습니다.", exception.getMessage());
    verify(orderRepository, never()).save(any());
  }
}
