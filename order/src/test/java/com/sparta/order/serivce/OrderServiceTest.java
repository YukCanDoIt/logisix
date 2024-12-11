package com.sparta.order.serivce;

import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.UnauthorizedException;
import com.sparta.order.repository.OrderRepository;
import com.sparta.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderService orderService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("주문 생성 성공")
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

    Order savedOrder = Order.builder()
        .id(UUID.randomUUID())
        .supplierId(request.getSupplierId())
        .receiverId(request.getReceiverId())
        .productId(request.getProductId())
        .quantity(request.getQuantity())
        .requestDetails(request.getRequestDetails())
        .deliveryId(UUID.randomUUID())
        .status(OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

    OrderResponse response = orderService.createOrder(request);

    assertNotNull(response);
    assertEquals(request.getSupplierId(), response.supplierId());
    assertEquals(OrderStatus.PENDING, response.status());
    verify(orderRepository, times(1)).save(any(Order.class));
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

    assertNotNull(response);
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
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("주문 삭제 실패 - 권한 부족")
  void deleteOrder_Unauthorized() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "SUPPLIER_USER";

    Order order = Order.builder()
        .id(orderId)
        .supplierId(UUID.randomUUID()) // 다른 사용자 ID로 설정
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
      orderService.deleteOrder(orderId, userId, role);
    });

    assertEquals("삭제 권한이 없습니다.", exception.getMessage());
    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("주문 삭제 성공 - MASTER_ADMIN 권한 사용")
  void deleteOrder_Success_WithRole() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "MASTER_ADMIN";

    Order order = Order.builder()
        .id(orderId)
        .supplierId(userId)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    orderService.deleteOrder(orderId, userId, role);

    verify(orderRepository, times(1)).save(order);
  }
}
