package com.sparta.slack.controller;

import com.sparta.slack.dto.SlackRequest;
import com.sparta.slack.dto.SlackResponse;
import com.sparta.slack.service.SlackService;
import com.sparta.order.dto.OrderRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/slack")
public class SlackController {

  private final SlackService slackService;

  public SlackController(SlackService slackService) {
    this.slackService = slackService;
  }

  @PostMapping("/send")
  public ResponseEntity<SlackResponse> sendSlackMessage(@RequestBody @Valid SlackRequest request) {
    try {
      OrderRequest orderRequest = createOrderRequest(request);
      String response = slackService.sendMessage(request, orderRequest);
      return ResponseEntity.ok(new SlackResponse(true, response, request.channel()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new SlackResponse(false, e.getMessage(), request.channel()));
    }
  }

  private OrderRequest createOrderRequest(SlackRequest request) {
    return new OrderRequest(
        UUID.fromString(request.orderSupplierId()),
        UUID.fromString(request.orderReceiverId()),
        UUID.fromString(request.orderHubId()),
        request.orderItems(),
        request.expectedDeliveryDate(),
        request.orderNote(),
        request.requestDetails()
    );
  }
}
