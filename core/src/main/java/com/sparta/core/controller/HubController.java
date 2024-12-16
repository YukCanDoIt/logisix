package com.sparta.core.controller;

import com.sparta.core.dto.HubRequest;
import com.sparta.core.dto.HubResponse;
import com.sparta.core.response.ApiResponse;
import com.sparta.core.service.HubService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/core/hubs")
@RequiredArgsConstructor
public class HubController {

  private final HubService hubService;

  @PostMapping
  public ResponseEntity createHub(@Valid @RequestBody HubRequest hubRequest) {
    hubService.createHub(hubRequest);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @GetMapping("/{hubId}")
  public ResponseEntity getHub(@PathVariable UUID hubId) {
    HubResponse hubResponse = hubService.getHub(hubId);
    return ResponseEntity.ok(ApiResponse.success(hubResponse));
  }

  @GetMapping
  public ResponseEntity getHubs(
      @RequestParam int size,
      @RequestParam String keyword,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") Direction direction,
      @RequestParam Integer page
  ) {
    Page<HubResponse> hubResponseDtoList = hubService.getHubs(size, keyword, direction,
        page - 1);
    return ResponseEntity.ok(ApiResponse.success(hubResponseDtoList));
  }

  @PutMapping("/{hubId}")
  public ResponseEntity updateHub(@PathVariable UUID hubId,
      @Valid @RequestBody HubRequest hubRequest) {
    hubService.updateHub(hubId, hubRequest);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @DeleteMapping("/{hubId}")
  public ResponseEntity deleteHub(@PathVariable UUID hubId) {
    hubService.deleteHub(hubId);
    return ResponseEntity.ok(ApiResponse.success());
  }
}