package com.sparta.core.controller;

import com.sparta.core.dto.HubRouteRequest;
import com.sparta.core.dto.HubRouteResponse;
import com.sparta.core.response.ApiResponse;
import com.sparta.core.service.HubRouteService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/core/hub-route")
@RequiredArgsConstructor
public class HubRouteController {

  private final HubRouteService hubRouteService;

  @PostMapping
  public ResponseEntity createHubRoutes() {
    hubRouteService.createHubRoutes();
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity getHubRoute(@RequestParam UUID arrivalHubId,
      @RequestParam UUID departureHubId) {
    HubRouteResponse hubRouteResponse = hubRouteService.getHubRoute(arrivalHubId, departureHubId);
    return ResponseEntity.ok(ApiResponse.success(hubRouteResponse));
  }
}
