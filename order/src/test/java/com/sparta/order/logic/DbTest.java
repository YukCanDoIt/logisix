package com.sparta.order.logic;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderItem;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.repository.OrderRepository;
import com.sparta.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DbTest {

  @Mock
  private DeliveryClient deliveryClient;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderService orderService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSaveOrderToDatabase() {
    // Given
    long supplierId = 100L;   // long 타입
    long receiverId = 200L;   // long 타입
    UUID hubId = UUID.randomUUID();
    UUID deliveryId = UUID.randomUUID();

    OrderItem orderItem = OrderItem.builder()
        .productId(UUID.randomUUID())
        .quantity(2)
        .pricePerUnit(1000)
        .build();

    OrderRequest orderRequest = new OrderRequest(
        supplierId,
        receiverId,
        hubId,
        List.of(orderItem.toRequest()),
        null,
        "Test order note",
        "Test request details"
    );

    String role = "MASTER";
    long userId = 12345L; // userId long 타입

    // Mock 설정
    when(deliveryClient.createDelivery(any(UUID.class), any(OrderRequest.class)))
        .thenReturn(deliveryId);

    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order savedOrder = invocation.getArgument(0);
      if (savedOrder.getId() == null) {
        savedOrder.setId(UUID.randomUUID()); // 패키지-프라이빗 setter 사용
      }
      return savedOrder;
    });

    // When
    OrderResponse savedOrderResponse = orderService.createOrder(orderRequest, userId, role);

    // 로그 추가 (디버깅 용도)
    System.out.println("Order ID: " + savedOrderResponse.orderId());

    // Then
    assertNotNull(savedOrderResponse, "OrderResponse should not be null");
    assertNotNull(savedOrderResponse.orderId(), "Order ID should not be null");
    assertNotNull(savedOrderResponse.supplierId(), "Supplier ID should not be null");
    assertNotNull(savedOrderResponse.receiverId(), "Receiver ID should not be null");
    assertNotNull(savedOrderResponse.hubId(), "Hub ID should not be null");
    assertNotNull(savedOrderResponse.status(), "Order status should not be null");
    assertNotNull(savedOrderResponse.deliveryId(), "Delivery ID should not be null");
    // 추가적인 필드 검증 예시
    assertNotNull(savedOrderResponse.orderNote(), "Order note should not be null");
    assertNotNull(savedOrderResponse.requestDetails(), "Request details should not be null");
  }
}
