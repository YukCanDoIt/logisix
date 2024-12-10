package com.sparta.order.serivce;

import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.repository.OrderRepository;
import com.sparta.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
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

  @InjectMocks
  private OrderService orderService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createOrder_Success() {
    // Given
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

    // When
    OrderResponse response = orderService.createOrder(request);

    // Then
    assertNotNull(response);
    assertEquals(request.getSupplierId(), response.supplierId());
    assertEquals(OrderStatus.PENDING, response.status());
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  void updateOrderStatus_Success() {
    // Given
    UUID orderId = UUID.randomUUID();
    String newStatus = "CONFIRMED";

    Order order = Order.builder()
        .id(orderId)
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    // When
    OrderResponse response = orderService.updateOrderStatus(orderId, newStatus);

    // Then
    assertNotNull(response);
    assertEquals(OrderStatus.CONFIRMED, response.status());
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  void updateOrderStatus_InvalidTransition() {
    // Given
    UUID orderId = UUID.randomUUID();
    Order order = Order.builder()
        .id(orderId)
        .status(OrderStatus.CONFIRMED)
        .build();
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    // When & Then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      orderService.updateOrderStatus(orderId, "PENDING");
    });

    assertEquals("CONFIRMED 상태에서 PENDING으로 변경할 수 없습니다.", exception.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void getOrdersByUser_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    Order order = Order.builder()
        .supplierId(userId)
        .isDelete(false)
        .status(OrderStatus.PENDING)
        .build();

    when(orderRepository.findAll()).thenReturn(List.of(order));

    // When
    List<OrderResponse> responses = orderService.getOrdersByUser(userId);

    // Then
    assertNotNull(responses);
    assertEquals(1, responses.size());
    assertEquals(userId, responses.get(0).supplierId());
    verify(orderRepository, times(1)).findAll();
  }
}
