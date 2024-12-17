package com.sparta.order.client;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserClient {

  @GetMapping("/{user_id}/role")
  Map<String, String> getUserRole(@PathVariable("user_id") long userId,
      @RequestHeader("Authorization") String token);
}
