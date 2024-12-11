package com.sparta.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.order.domain.OrderStatus;
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
    mockMvc = MockMvcBuilders.standaloneSetup(orderController)
        .setControllerAdvice(new GlobalExceptionHandler()) // 예외 처리기 추가
        .addFilters(new CharacterEncodingFilter("UTF-8", true)) // UTF-8 필터 추가
        .build();
  }

  @Test
  @DisplayName("주문 생성 성공 테스트")
  void createOrder_Success() throws Exception {
    // Given
    OrderRequest request = new OrderRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        10,
        "Urgent delivery",
        "Source Hub",
        "Destination Hub",
        OrderStatus.PENDING
    );

    OrderResponse response = new OrderResponse(
        UUID.randomUUID(),
        request.getSupplierId(),
        request.getReceiverId(),
        request.getProductId(),
        request.getQuantity(),
        request.getRequestDetails(),
        UUID.randomUUID(),
        false,
        OrderStatus.PENDING
    );

    when(orderService.createOrder(any())).thenReturn(response);

    // When & Then
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").isNotEmpty())
        .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));

    verify(orderService, times(1)).createOrder(any());
  }

  @Test
  @DisplayName("주문 상태 변경 성공 - MASTER_ADMIN 권한 사용")
  void updateOrderStatus_Success_WithRole() throws Exception {
    // Given
    UUID orderId = UUID.randomUUID();
    String newStatus = "CONFIRMED";
    String role = "MASTER_ADMIN";

    OrderResponse response = new OrderResponse(
        orderId,
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        10,
        "Urgent delivery",
        UUID.randomUUID(),
        false,
        OrderStatus.CONFIRMED
    );

    when(orderService.updateOrderStatus(orderId, newStatus)).thenReturn(response);

    // When & Then
    mockMvc.perform(patch("/api/orders/{id}/status", orderId.toString())
            .param("status", newStatus)
            .header("X-User-Role", role)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.name()));

    verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
  }

  @Test
  @DisplayName("주문 상태 변경 실패 - 권한 부족 (SUPPLIER_USER)")
  void updateOrderStatus_Unauthorized() throws Exception {
    // Given
    UUID orderId = UUID.randomUUID();
    String newStatus = "CONFIRMED";
    String role = "SUPPLIER_USER"; // 권한 없는 역할

    // When & Then
    mockMvc.perform(patch("/api/orders/{id}/status", orderId.toString())
            .param("status", newStatus)
            .header("X-User-Role", role)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // JSON Content-Type 검증
        .andExpect(jsonPath("$.message").value("상태 변경 권한이 없습니다.")); // JSON 메시지 검증
  }


  @Test
  @DisplayName("주문 삭제 성공 - MASTER_ADMIN 권한 사용")
  void deleteOrder_Success_WithRole() throws Exception {
    // Given
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "MASTER_ADMIN";

    doNothing().when(orderService).deleteOrder(orderId, userId, role);

    // When & Then
    mockMvc.perform(delete("/api/orders/{id}", orderId.toString())
            .header("X-User-ID", userId.toString())
            .header("X-User-Role", role))
        .andExpect(status().isNoContent());

    verify(orderService, times(1)).deleteOrder(orderId, userId, role);
  }

  @Test
  @DisplayName("주문 삭제 실패 - 권한 부족 (SUPPLIER_USER)")
  void deleteOrder_Unauthorized() throws Exception {
    // Given
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String role = "SUPPLIER_USER"; // 권한 없는 역할

    doThrow(new UnauthorizedException("삭제 권한이 없습니다."))
        .when(orderService).deleteOrder(orderId, userId, role);

    // When & Then
    mockMvc.perform(delete("/api/orders/{id}", orderId.toString())
            .header("X-User-ID", userId.toString())
            .header("X-User-Role", role))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // JSON Content-Type 검증
        .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다.")); // JSON 메시지 검증
  }

}
