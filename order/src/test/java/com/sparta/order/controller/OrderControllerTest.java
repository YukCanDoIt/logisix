package com.sparta.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.order.domain.OrderStatus;
import com.sparta.order.dto.OrderRequest;
import com.sparta.order.dto.OrderResponse;
import com.sparta.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
  }

  @Test
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
  void updateOrderStatus_Success() throws Exception {
    // Given
    UUID orderId = UUID.randomUUID();
    String newStatus = "CONFIRMED";

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
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.name()));

    verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
  }
}
