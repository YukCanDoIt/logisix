package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.UnauthorizedException;
import com.sparta.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final DeliveryClient deliveryClient;

  public OrderService(OrderRepository orderRepository, DeliveryClient deliveryClient) {
    this.orderRepository = orderRepository;
    this.deliveryClient = deliveryClient;
  }

  // 주문 생성
  @Transactional
  public OrderResponse createOrder(OrderRequest orderRequest) {
    // Order를 먼저 저장하여 ID를 생성
    Order order = Order.builder()
        .supplierId(orderRequest.getSupplierId())
        .receiverId(orderRequest.getReceiverId())
        .productId(orderRequest.getProductId())
        .quantity(orderRequest.getQuantity())
        .requestDetails(orderRequest.getRequestDetails())
        .status(OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    orderRepository.save(order);

    // 생성된 Order ID로 배송 요청
    UUID deliveryId = deliveryClient.createDelivery(order.getId(), orderRequest);

    // 배송 ID 업데이트
    order.setDeliveryId(deliveryId);
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 본인 주문 조회
  @Transactional(readOnly = true)
  public List<OrderResponse> getOrdersByUser(UUID userId, String role) {
    return orderRepository.findAll().stream()
        .filter(order -> isAccessible(userId, role, order) && !order.isDelete()) // 삭제된 데이터 제외
        .map(this::mapToOrderResponse)
        .collect(Collectors.toList());
  }

  // 본인 주문 수정
  @Transactional
  public OrderResponse updateOrder(UUID id, UUID userId, OrderRequest orderRequest, String role) {
    Order order = orderRepository.findById(id)
        .filter(o -> isAccessible(userId, role, o) && !o.isDelete()) // 권한과 삭제 상태 검증
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없거나 액세스가 거부되었습니다."));

    order.setQuantity(orderRequest.getQuantity());
    order.setRequestDetails(orderRequest.getRequestDetails());
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 본인 주문 삭제
  @Transactional
  public void deleteOrder(UUID id, UUID userId, String role) {
    Order order = orderRepository.findById(id)
        .filter(o -> !o.isDelete()) // 삭제된 데이터 제외
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

    if (!"MASTER_ADMIN".equals(role) && !order.getSupplierId().equals(userId)) {
      throw new UnauthorizedException("삭제 권한이 없습니다.");
    }

    order.markAsDeleted(); // 논리적 삭제
    orderRepository.save(order);
  }

  // 주문 상태 변경
  @Transactional
  public OrderResponse updateOrderStatus(UUID id, String newStatus) {
    Order order = orderRepository.findById(id)
        .filter(o -> !o.isDelete()) // 삭제된 데이터 제외
        .orElseThrow(() -> new IllegalArgumentException("ID가 있는 주문을 찾을 수 없습니다.: " + id));

    OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());

    if (order.getStatus() == OrderStatus.CONFIRMED && status == OrderStatus.PENDING) {
      throw new IllegalStateException("CONFIRMED 상태에서 PENDING으로 변경할 수 없습니다.");
    }

    order.setStatus(status);
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 권한 확인 로직
  private boolean isAccessible(UUID userId, String role, Order order) {
    if ("MASTER_ADMIN".equals(role)) {
      return true; // 마스터 관리자는 모든 주문 접근 가능
    } else if ("HUB_ADMIN".equals(role)) {
      return order.getSupplierId().equals(userId); // 허브 관리자는 본인 허브의 주문만 접근 가능
    } else {
      return order.getSupplierId().equals(userId); // 기타 사용자: 본인 주문만 접근 가능
    }
  }

  // Order -> OrderResponse 매핑 메서드
  private OrderResponse mapToOrderResponse(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getSupplierId(),
        order.getReceiverId(),
        order.getProductId(),
        order.getQuantity(),
        order.getRequestDetails(),
        order.getDeliveryId(),
        order.isDelete(),
        order.getStatus()
    );
  }
}
