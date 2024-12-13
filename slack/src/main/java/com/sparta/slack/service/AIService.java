package com.sparta.slack.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIService {

  private final RestTemplate restTemplate;

  // 생성자 주입
  public AIService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // AI에 발송 시한 계산 요청
  public String calculateFinalSendDeadline(String orderDetails) {
    String aiApiUrl = "https://api.gemini.com/v1beta/models";  // AI API URL

    // 요청에 필요한 데이터 전송 (예: 상품, 수량, 요청 사항 등)
    String response = restTemplate.postForObject(aiApiUrl, orderDetails, String.class);

    return response;  // AI가 반환하는 최종 발송 시한 정보
  }
}
