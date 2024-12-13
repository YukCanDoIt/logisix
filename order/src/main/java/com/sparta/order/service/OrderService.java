package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderItem;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemResponse;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.UnauthorizedException;
import com.sparta.order.repository.OrderRepository;
import com.sparta.user.entity.Role;  // Role Enum을 import
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
    validateOrderRequest(orderRequest);

    Order order = Order.builder()
        .supplierId(orderRequest.supplierId())
        .receiverId(orderRequest.receiverId())
        .hubId(orderRequest.hubId())
        .orderItems(orderRequest.orderItems().stream()
            .map(item -> OrderItem.builder()
                .productId(item.productId())
                .quantity(item.quantity())  // 여기서 quantity를 사용
                .pricePerUnit(item.pricePerUnit())
                .build())
            .collect(Collectors.toList()))
        .expectedDeliveryDate(orderRequest.expectedDeliveryDate())
        .orderNote(orderRequest.orderNote())
        .status(OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .build();

    orderRepository.save(order);

    UUID deliveryId = deliveryClient.createDelivery(order.getId(), orderRequest);
    order.setDeliveryId(deliveryId);
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 사용자별 주문 조회
  @Transactional(readOnly = true)
  public List<OrderResponse> getOrdersByUser(UUID userId, String role) {
    Role userRole = parseRole(role);  // Role Enum으로 변환

    return orderRepository.findAll().stream()
        .filter(order -> isAccessible(userId, userRole, order) && !order.isDelete())
        .map(this::mapToOrderResponse)
        .collect(Collectors.toList());
  }

  // 주문 수정
  @Transactional
  public OrderResponse updateOrder(UUID id, UUID userId, OrderRequest orderRequest, String role) {
    Role userRole = parseRole(role);  // Role Enum으로 변환

    Order order = orderRepository.findById(id)
        .filter(o -> isAccessible(userId, userRole, o) && !o.isDelete())
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

    List<OrderItem> updatedItems = orderRequest.orderItems().stream()
        .map(item -> OrderItem.builder()
            .productId(item.productId())
            .quantity(item.quantity())  // quantity를 사용
            .pricePerUnit(item.pricePerUnit())
            .build())
        .collect(Collectors.toList());

    order.updateOrder(updatedItems, orderRequest.orderNote(), orderRequest.expectedDeliveryDate());
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 주문 삭제 (논리 삭제)
  @Transactional
  public void deleteOrder(UUID id, UUID userId, String role) {
    Role userRole = parseRole(role);  // Role Enum으로 변환

    Order order = orderRepository.findById(id)
        .filter(o -> !o.isDelete())
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

    if (!isAccessible(userId, userRole, order)) {
      throw new UnauthorizedException("삭제 권한이 없습니다.");
    }

    order.markAsDeleted();
    orderRepository.save(order);
  }

  // 주문 상태 변경
  @Transactional
  public OrderResponse updateOrderStatus(UUID id, String newStatus) {
    Order order = orderRepository.findById(id)
        .filter(o -> !o.isDelete())
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

    if (order.getIsDeliveryStarted()) {
      throw new IllegalStateException("배송이 시작된 후에는 상태를 변경할 수 없습니다.");
    }

    OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
    order.setStatus(status);
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 필수 필드 검증
  private void validateOrderRequest(OrderRequest orderRequest) {
    if (orderRequest.supplierId() == null || orderRequest.receiverId() == null
        || orderRequest.hubId() == null || orderRequest.orderItems().isEmpty()) {
      throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
    }
  }

  // 권한 검증
  private boolean isAccessible(UUID userId, Role role, Order order) {
    if (role == Role.MASTER) {
      return true;
    } else if (role == Role.HUB_MANAGER) {
      return order.getHubId().equals(userId); // 담당 허브 관리자의 권한 체크
    } else if (role == Role.DELIVERER || role == Role.COMPANY_MANAGER) {
      return order.getSupplierId().equals(userId); // 배송 담당자 또는 업체 담당자의 권한 체크
    }
    return false;
  }

  // Role Enum 값으로 변환하는 메서드
  private Role parseRole(String role) {
    try {
      return Role.valueOf(role);  // Role Enum으로 변환
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("잘못된 권한 값입니다.");  // 예외 처리
    }
  }

  // Order -> OrderResponse 매핑
  private OrderResponse mapToOrderResponse(Order order) {
    List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
        .map(item -> new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getPricePerUnit()))
        .collect(Collectors.toList());

    return new OrderResponse(
        order.getId(),
        order.getSupplierId(),
        order.getReceiverId(),
        order.getHubId(),
        orderItemResponses,
        order.getOrderNote(), // orderNote 추가
        order.getStatus(),
        order.getDeliveryId(), // deliveryId 추가
        order.getRequestDetails() // requestDetails 추가
    );
  }
}
