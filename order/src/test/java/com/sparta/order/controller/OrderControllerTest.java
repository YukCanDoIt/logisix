package com.sparta.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderItemRequest;
import com.sparta.order.dto.OrderItemResponse;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.exception.GlobalExceptionHandler;
import com.sparta.order.exception.UnauthorizedException;
import com.sparta.order.service.OrderService;
import com.sparta.order.client.UserClient;

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
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

  private MockMvc mockMvc;

  @Mock
  private OrderService orderService;

  @Mock
  private UserClient userClient; // UserClient Mock 추가

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
    UUID userId = UUID.randomUUID();
    String token = "Bearer test-token";

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

    // UserClient 역할 반환 Mock 설정
    when(userClient.getUserRole(eq(userId), eq(token)))
        .thenReturn(Map.of("role", "MASTER"));
    when(orderService.createOrder(any(OrderRequest.class), eq(userId), eq("MASTER"))).thenReturn(response);

    mockMvc.perform(post("/api/orders")
            .header("X-User-ID", userId.toString())
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId.toString()))
        .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));

    verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(userId), eq("MASTER"));
  }

  @Test
  @DisplayName("주문 조회 성공 테스트")
  void getMyOrders_Success() throws Exception {
    UUID userId = UUID.randomUUID();
    String token = "Bearer test-token";

    OrderResponse response = new OrderResponse(
        UUID.randomUUID(), userId, UUID.randomUUID(), UUID.randomUUID(),
        Collections.emptyList(), "Order note", OrderStatus.PENDING,
        UUID.randomUUID(), "Request details"
    );

    when(userClient.getUserRole(eq(userId), eq(token)))
        .thenReturn(Map.of("role", "COMPANY_MANAGER"));
    when(orderService.getOrdersByUser(eq(userId), eq("COMPANY_MANAGER"))).thenReturn(List.of(response));

    mockMvc.perform(get("/api/orders/my")
            .header("X-User-ID", userId.toString())
            .header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(1));

    verify(orderService, times(1)).getOrdersByUser(eq(userId), eq("COMPANY_MANAGER"));
  }

  @Test
  @DisplayName("주문 삭제 실패 - 권한 없음")
  void deleteOrder_Fail_Unauthorized() throws Exception {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String token = "Bearer test-token";

    when(userClient.getUserRole(eq(userId), eq(token)))
        .thenReturn(Map.of("role", "DELIVERER")); // 권한이 부족함

    doThrow(new UnauthorizedException("삭제 권한이 없습니다."))
        .when(orderService).deleteOrder(eq(orderId), eq(userId), eq("DELIVERER"));

    mockMvc.perform(delete("/api/orders/{id}", orderId)
            .header("X-User-ID", userId.toString())
            .header("Authorization", token))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다."));

    verify(orderService, times(1)).deleteOrder(eq(orderId), eq(userId), eq("DELIVERER"));
  }
}
