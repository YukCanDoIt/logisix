package com.sparta.slack.service;

import com.sparta.slack.dto.GeminiRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeminiService {

  @Value("${ai.api.url}")
  private String apiUrl;

  private final RestTemplate restTemplate;

  public GeminiService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String getAiResponse(String message) {
    // API URL과 경로 설정
    String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
        .path("/generateContent")
        .toUriString();

    // GeminiRequest 객체 생성
    GeminiRequest geminiRequest = new GeminiRequest(message);

    // AI 응답 요청
    String response = restTemplate.postForObject(url, geminiRequest, String.class);

    return response;  // AI의 응답 메시지 반환
  }
}
