package com.sparta.delivery.controller;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.GetDelivererResponse;
import com.sparta.delivery.dto.RegisterDelivererRequest;
import com.sparta.delivery.dto.UpdateDelivererRequest;
import com.sparta.delivery.service.DelivererService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deliveries/deliverer")
public class DelivererController {

    private final DelivererService delivererService;

    // 배송 담당자 추가
    @PostMapping
    public ApiResponse<Void> registerDeliverer(
            @RequestBody RegisterDelivererRequest request
    ) {
        return delivererService.registerDeliverer(request);
    }

    // 배송 담당자 단건 조회
    @GetMapping("/{delivererId}")
    public ApiResponse<GetDelivererResponse> getDeliverer(
            @PathVariable Long delivererId
    ) {
        return delivererService.getDeliverer(delivererId);
    }

    // 배송 담당자 정보 수정
    @PatchMapping("/{delivererId}")
    public ApiResponse<GetDelivererResponse> updateDeliverer(
            @PathVariable Long delivererId,
            @RequestBody UpdateDelivererRequest request
    ) {
        return delivererService.updateDeliverer(delivererId, request);
    }
}
