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
        .setControllerAdvice(new GlobalExceptionHandler()) // UnauthorizedException 처리 핸들러 추가됨
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  @Test
  @DisplayName("주문 생성 성공 테스트")
  void createOrder_Success() throws Exception {
    long userId = 12345L;
    long supplierId = 100L;
    long receiverId = 200L;
    UUID hubId = UUID.randomUUID();
    String token = "Bearer test-token";

    OrderItemRequest itemRequest = new OrderItemRequest(
        UUID.randomUUID(), 10, 500
    );

    OrderRequest request = new OrderRequest(
        supplierId, receiverId, hubId,
        Collections.singletonList(itemRequest), LocalDateTime.now(),
        "Test order note", "Test request details"
    );

    UUID orderId = UUID.randomUUID();
    OrderResponse response = new OrderResponse(
        orderId, supplierId, receiverId,
        hubId, Collections.singletonList(new OrderItemResponse(
        itemRequest.productId(), itemRequest.quantity(), itemRequest.pricePerUnit())),
        request.orderNote(), OrderStatus.PENDING, UUID.randomUUID(),
        request.requestDetails()
    );

    when(orderService.createOrder(any(OrderRequest.class), eq(userId), eq("MASTER"))).thenReturn(response);

    mockMvc.perform(post("/api/v1/orders")
            .header("X-User-ID", String.valueOf(userId))
            .header("X-User-Role", "MASTER")
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
    long userId = 12345L;
    UUID hubId = UUID.randomUUID();
    String token = "Bearer test-token";

    long supplierIdLong = 100L;
    long receiverIdLong = 200L;

    OrderResponse response = new OrderResponse(
        UUID.randomUUID(), supplierIdLong, receiverIdLong, hubId,
        Collections.emptyList(), "Order note", OrderStatus.PENDING,
        UUID.randomUUID(), "Request details"
    );

    when(orderService.getOrdersByUser(userId, "COMPANY_MANAGER")).thenReturn(List.of(response));

    mockMvc.perform(get("/api/v1/orders/my")
            .header("X-User-ID", String.valueOf(userId))
            .header("X-User-Role", "COMPANY_MANAGER")
            .header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(1));

    verify(orderService, times(1)).getOrdersByUser(userId, "COMPANY_MANAGER");
  }

  @Test
  @DisplayName("주문 삭제 실패 - 권한 없음")
  void deleteOrder_Fail_Unauthorized() throws Exception {
    long userId = 12345L;
    UUID orderId = UUID.randomUUID();
    String token = "Bearer test-token";

    // UnauthorizedException 발생 시 GlobalExceptionHandler에서 403 반환
    doThrow(new UnauthorizedException("삭제 권한이 없습니다."))
        .when(orderService).deleteOrder(eq(orderId), eq(userId), eq("DELIVERER"));

    mockMvc.perform(delete("/api/v1/orders/{id}", orderId)
            .header("X-User-ID", String.valueOf(userId))
            .header("X-User-Role", "DELIVERER")
            .header("Authorization", token))
        .andExpect(status().isForbidden()) // 이제 403 기대
        .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다."));

    verify(orderService, times(1)).deleteOrder(eq(orderId), eq(userId), eq("DELIVERER"));
  }
}
