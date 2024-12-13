package com.sparta.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderItemResponse;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.GlobalExceptionHandler;
import com.sparta.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

  private MockMvc mockMvc;

  @Mock
  private OrderService orderService;

  @InjectMocks
  private OrderController orderController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    objectMapper.registerModule(new JavaTimeModule());

    mockMvc = MockMvcBuilders.standaloneSetup(orderController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  @Test
  @DisplayName("주문 생성 성공 테스트")
  void createOrder_Success() throws Exception {
    UUID orderId = UUID.randomUUID();
    OrderItemRequest itemRequest = new OrderItemRequest(
        UUID.randomUUID(), 10, 500
    );

    OrderRequest request = new OrderRequest(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        Collections.singletonList(itemRequest), LocalDateTime.now(),
        "Test order note", "Test request details"
    );

    OrderResponse response = new OrderResponse(
        orderId, request.supplierId(), request.receiverId(),
        request.hubId(), Collections.singletonList(new OrderItemResponse(
        itemRequest.productId(), itemRequest.quantity(), itemRequest.pricePerUnit())),
        request.orderNote(), OrderStatus.PENDING, UUID.randomUUID(),
        request.requestDetails()
    );

    when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId.toString()))
        .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));

    verify(orderService, times(1)).createOrder(any(OrderRequest.class));
  }

  @Test
  @DisplayName("주문 조회 성공 테스트")
  void getMyOrders_Success() throws Exception {
    UUID userId = UUID.randomUUID();
    String role = "COMPANY_MANAGER";

    OrderResponse response = new OrderResponse(
        UUID.randomUUID(), userId, UUID.randomUUID(), UUID.randomUUID(),
        Collections.emptyList(), "Order note", OrderStatus.PENDING,
        UUID.randomUUID(), "Request details"
    );

    when(orderService.getOrdersByUser(eq(userId), eq(role))).thenReturn(List.of(response));

    mockMvc.perform(get("/api/orders/my")
            .header("X-User-ID", userId.toString())
            .header("X-User-Role", role))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(1));

    verify(orderService, times(1)).getOrdersByUser(eq(userId), eq(role));
  }

  @Test
  @DisplayName("주문 수정 성공 테스트")
  void updateOrder_Success() throws Exception {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "COMPANY_MANAGER";

    OrderItemRequest itemRequest = new OrderItemRequest(
        UUID.randomUUID(), 15, 600
    );

    OrderRequest request = new OrderRequest(
        userId, UUID.randomUUID(), UUID.randomUUID(),
        Collections.singletonList(itemRequest), LocalDateTime.now(),
        "Updated order note", "Updated request details"
    );

    OrderResponse response = new OrderResponse(
        orderId, userId, request.receiverId(), request.hubId(),
        Collections.singletonList(new OrderItemResponse(
            itemRequest.productId(), itemRequest.quantity(), itemRequest.pricePerUnit()
        )), request.orderNote(), OrderStatus.PENDING, UUID.randomUUID(),
        request.requestDetails()
    );

    when(orderService.updateOrder(eq(orderId), eq(userId), any(OrderRequest.class), eq(role)))
        .thenReturn(response);

    mockMvc.perform(patch("/api/orders/{id}", orderId)
            .header("X-User-ID", userId.toString())
            .header("X-User-Role", role)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantity").value(15))
        .andExpect(jsonPath("$.orderNote").value("Updated order note"));

    verify(orderService, times(1)).updateOrder(eq(orderId), eq(userId), any(OrderRequest.class), eq(role));
  }

  @Test
  @DisplayName("주문 삭제 성공 테스트")
  void deleteOrder_Success() throws Exception {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "COMPANY_MANAGER";

    doNothing().when(orderService).deleteOrder(eq(orderId), eq(userId), eq(role));

    mockMvc.perform(delete("/api/orders/{id}", orderId)
            .header("X-User-ID", userId.toString())
            .header("X-User-Role", role))
        .andExpect(status().isNoContent());

    verify(orderService, times(1)).deleteOrder(eq(orderId), eq(userId), eq(role));
  }

  @Test
  @DisplayName("주문 상태 변경 성공 테스트")
  void updateOrderStatus_Success() throws Exception {
    UUID orderId = UUID.randomUUID();
    String role = "COMPANY_MANAGER";
    String newStatus = "CONFIRMED";

    OrderResponse response = new OrderResponse(
        orderId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        Collections.emptyList(), "Order note", OrderStatus.CONFIRMED,
        UUID.randomUUID(), "Request details"
    );

    when(orderService.updateOrderStatus(eq(orderId), eq(newStatus))).thenReturn(response);

    mockMvc.perform(patch("/api/orders/{id}/status", orderId)
            .header("X-User-Role", role)
            .param("status", newStatus))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.name()));

    verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(newStatus));
  }

  // 추가 테스트 코드 작성 (?)


}
