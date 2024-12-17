package com.sparta.slack.client;

import com.sparta.slack.dto.OrderItemRequest;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "order-service", url = "${ORDER_SERVICE_URL}")
public interface OrderClient {

  @PostMapping("/api/orders/validate")
  Map<String, Object> validateOrder(@RequestBody List<OrderItemRequest> orderItems);
}
