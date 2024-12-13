package com.sparta.order.logic;

import com.sparta.order.domain.OrderItem;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.repository.OrderRepository;
import com.sparta.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DbTest {

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void testSaveOrderToDatabase() {
    // Given: 주문 생성
    UUID supplierId = UUID.randomUUID(); // 공급자 ID
    UUID receiverId = UUID.randomUUID(); // 수신자 ID
    UUID hubId = UUID.randomUUID(); // 허브 ID

    // 주문 아이템 생성
    OrderItem orderItem = OrderItem.builder()
        .productId(UUID.randomUUID()) // 제품 ID
        .quantity(2)  // 수량
        .pricePerUnit(1000)  // 단가
        .build();

    // 주문 요청 생성
    OrderRequest orderRequest = new OrderRequest(
        supplierId,
        receiverId,
        hubId,
        List.of(),  // 주문 아이템
        null,  // 예상 배송 날짜는 null로 설정
        "Test order note",  // 주문 노트
        "Test request details"  // 요청 세부사항
    );

    // When: 주문 저장
    OrderResponse savedOrder = orderService.createOrder(orderRequest);

    // Then: 주문이 제대로 저장되었는지 확인
    assertNotNull(savedOrder);
    assertNotNull(savedOrder.orderId(), "ID should not be null after saving.");
    assertEquals(supplierId, savedOrder.supplierId());
    assertEquals(receiverId, savedOrder.receiverId());
    assertEquals(hubId, savedOrder.hubId());
    assertEquals("Test order note", savedOrder.orderNote());
    assertEquals(2, savedOrder.quantity());  // 주문 아이템의 수량 합
    assertEquals("Test request details", savedOrder.requestDetails());
    assertEquals(OrderStatus.PENDING, savedOrder.status());  // 상태가 PENDING이어야 함

    // Clean up: 테스트 후 데이터 삭제
    orderRepository.deleteById(savedOrder.orderId());
  }
}
