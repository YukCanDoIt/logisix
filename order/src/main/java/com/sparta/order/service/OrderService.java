package com.sparta.order.service;

import com.sparta.order.client.DeliveryClient;
import com.sparta.order.client.UserClient;
import com.sparta.order.domain.Order;
import com.sparta.order.domain.OrderItem;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemResponse;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.ErrorCode;
import com.sparta.order.exception.LogisixException; // 글로벌 예외 처리용 예외
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
  public OrderResponse createOrder(OrderRequest orderRequest, long userId, String role) {
    validateOrderRequest(orderRequest);

    if (!"MASTER".equals(role)) {
      throw new LogisixException(ErrorCode.FORBIDDEN_ACCESS); // 권한 없음
    }

    Order order = Order.create(orderRequest);
    orderRepository.save(order);

    UUID deliveryId = deliveryClient.createDelivery(order.getId(), orderRequest);
    order.setDeliveryId(deliveryId);
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 주문 삭제
  @Transactional
  public void deleteOrder(UUID id, long userId, String role) {
    Order order = orderRepository.findById(id)
        .filter(o -> !o.isDeleted())
        .orElseThrow(() -> new LogisixException(ErrorCode.INVALID_REQUEST_DATA)); // 주문 없음

    if (!"MASTER".equals(role)) {
      throw new LogisixException(ErrorCode.FORBIDDEN_ACCESS); // 삭제 권한 없음
    }

    order.markAsDeleted();
    orderRepository.save(order);
  }

  // 사용자별 주문 조회
  @Transactional(readOnly = true)
  public List<OrderResponse> getOrdersByUser(long userId, String role) {
    return orderRepository.findAll().stream()
        .filter(order -> isAccessible(userId, role, order) && !order.isDeleted())
        .map(this::mapToOrderResponse)
        .collect(Collectors.toList());
  }

  // 주문 수정
  @Transactional
  public OrderResponse updateOrder(UUID id, long userId, OrderRequest orderRequest, String role) {
    Order order = orderRepository.findById(id)
        .filter(o -> isAccessible(userId, role, o) && !o.isDeleted())
        .orElseThrow(() -> new LogisixException(ErrorCode.INVALID_REQUEST_DATA)); // 주문 없음 또는 접근 불가

    List<OrderItem> updatedItems = orderRequest.orderItems().stream()
        .map(item -> new OrderItem(item.productId(), item.quantity(), item.pricePerUnit()))
        .collect(Collectors.toList());

    order.updateOrder(updatedItems, orderRequest.orderNote(), orderRequest.expectedDeliveryDate());
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 주문 상태 변경
  @Transactional
  public OrderResponse updateOrderStatus(UUID id, String newStatus, String role) {
    // UUID 타입으로 변경
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new LogisixException(ErrorCode.INVALID_REQUEST_DATA)); // 주문 없음

    if (order.getStatus() == OrderStatus.CONFIRMED) {
      throw new LogisixException(ErrorCode.INVALID_REQUEST_DATA); // 배송 시작 후 변경 불가
    }

    order.setStatus(OrderStatus.valueOf(newStatus.toUpperCase()));
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  // 필수 필드 검증
  private void validateOrderRequest(OrderRequest orderRequest) {
    if (orderRequest.hubId() == null
        || orderRequest.orderItems() == null
        || orderRequest.orderItems().isEmpty()) {
      throw new LogisixException(ErrorCode.INVALID_REQUEST_DATA); // 필수 필드 누락
    }
  }

  // 접근 권한 검증
  private boolean isAccessible(long userId, String role, Order order) {
    return switch (role) {
      case "MASTER" -> true;
      case "HUB_MANAGER" -> {
        // hubId는 UUID, userId는 long이므로 직접 비교 불가능
        // TODO: 기획서 기반으로 userId -> hubId 매핑 로직 구현 필요
        yield false;
      }
      case "DELIVERER", "COMPANY_MANAGER" -> order.getSupplierId() == userId;
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
