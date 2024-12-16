package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.client.UserClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderItem;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemResponse;
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
  private final UserClient userClient;

  public OrderService(OrderRepository orderRepository, DeliveryClient deliveryClient, UserClient userClient) {
    this.orderRepository = orderRepository;
    this.deliveryClient = deliveryClient;
    this.userClient = userClient;
  }

  // 주문 생성
  @Transactional
  public OrderResponse createOrder(OrderRequest orderRequest, UUID userId, String token) {
    String role = getUserRole(userId, token);
    if (!"MASTER".equals(role)) {
      throw new UnauthorizedException("권한이 없습니다. 주문 생성은 MASTER만 가능합니다.");
    }

    // 주문 생성
    Order order = Order.builder()
        .supplierId(orderRequest.supplierId())
        .receiverId(orderRequest.receiverId())
        .hubId(orderRequest.hubId())
        .orderItems(orderRequest.orderItems().stream()
            .map(item -> new OrderItem(item.productId(), item.quantity(), item.pricePerUnit()))
            .collect(Collectors.toList()))
        .expectedDeliveryDate(orderRequest.expectedDeliveryDate())
        .orderNote(orderRequest.orderNote())
        .status(OrderStatus.PENDING)
        .build();

    orderRepository.save(order);

    // 배송 생성
    UUID deliveryId = deliveryClient.createDelivery(order.getId(), orderRequest);
    order.setDeliveryId(deliveryId);
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 사용자별 주문 조회
  @Transactional(readOnly = true)
  public List<OrderResponse> getOrdersByUser(UUID userId, String token) {
    String role = getUserRole(userId, token);

    return orderRepository.findAll().stream()
        .filter(order -> isAccessible(userId, role, order) && !order.isDeleted())
        .map(this::mapToOrderResponse)
        .collect(Collectors.toList());
  }

  // 주문 수정
  @Transactional
  public OrderResponse updateOrder(UUID id, UUID userId, OrderRequest orderRequest, String token) {
    String role = getUserRole(userId, token);
    Order order = orderRepository.findById(id)
        .filter(o -> isAccessible(userId, role, o) && !o.isDeleted())
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

    List<OrderItem> updatedItems = orderRequest.orderItems().stream()
        .map(item -> new OrderItem(item.productId(), item.quantity(), item.pricePerUnit()))
        .collect(Collectors.toList());

    order.updateOrder(updatedItems, orderRequest.orderNote(), orderRequest.expectedDeliveryDate());
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 주문 삭제
  @Transactional
  public void deleteOrder(UUID id, UUID userId, String token) {
    String role = getUserRole(userId, token);
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

    if (!"MASTER".equals(role)) {
      throw new UnauthorizedException("삭제 권한이 없습니다.");
    }

    order.markAsDeleted();
    orderRepository.save(order);
  }

  // 주문 상태 변경
  @Transactional
  public OrderResponse updateOrderStatus(UUID id, String newStatus, String token) {
    String role = getUserRole(id, token);
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

    if (order.getStatus() == OrderStatus.CONFIRMED) {
      throw new IllegalStateException("배송이 시작된 후에는 상태를 변경할 수 없습니다.");
    }

    order.setStatus(OrderStatus.valueOf(newStatus.toUpperCase()));
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 사용자 역할 조회 메서드
  private String getUserRole(UUID userId, String token) {
    return userClient.getUserRole(userId, token).get("role");
  }

  // 접근 권한 검증
  private boolean isAccessible(UUID userId, String role, Order order) {
    return switch (role) {
      case "MASTER" -> true;
      case "HUB_MANAGER" -> order.getHubId().equals(userId);
      case "DELIVERER", "COMPANY_MANAGER" -> order.getSupplierId().equals(userId);
      default -> false;
    };
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
        order.getOrderNote(),
        order.getStatus(),
        order.getDeliveryId(),
        order.getRequestDetails()
    );
  }
}
