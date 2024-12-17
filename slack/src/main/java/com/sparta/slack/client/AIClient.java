package com.sparta.slack.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "aiClient", url = "${ai.api.url}")
public interface AIClient {

  @PostMapping("${ai.api.calculate-deadline}")
  String calculateDeadline(@RequestBody Map<String, Object> payload);

  @PostMapping("${ai.api.generate-content}")
  String generateContent(@RequestBody Object payload);
}
