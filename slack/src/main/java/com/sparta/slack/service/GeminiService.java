package com.sparta.slack.service;

import com.sparta.slack.dto.GeminiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {

  private final AIClientService aiClientService;

  public String getAiResponse(String message) {
    GeminiRequest geminiRequest = new GeminiRequest(message);
    // 공통 서비스 호출
    return aiClientService.sendRequest(geminiRequest, "/generateContent");
  }
}
