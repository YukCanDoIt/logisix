package com.sparta.slack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIClientService {

  private final RestTemplate restTemplate;

  @Value("${ai.api.url}")
  private String aiApiUrl;

  public AIClientService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // 공통 API 호출 메서드
  public String sendRequest(Object payload, String endpoint) {
    String url = aiApiUrl + endpoint;
    try {
      return restTemplate.postForObject(url, payload, String.class);
    } catch (Exception e) {
      throw new RuntimeException("AI API 호출 중 오류 발생: " + e.getMessage());
    }
  }
}
