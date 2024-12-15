package com.sparta.slack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {

  private final AIClientService aiClientService;

  public String calculateFinalSendDeadline(String orderDetails) {
    // 공통 서비스 호출
    return aiClientService.sendRequest(orderDetails, "/finalDeadline");
  }
}
