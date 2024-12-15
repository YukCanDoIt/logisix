package com.sparta.delivery.controller;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.CreateDeliveryRequest;
import com.sparta.delivery.dto.GetDeliveryResponse;
import com.sparta.delivery.dto.ChangeDelivererRequest;
import com.sparta.delivery.dto.UpdateDeliveryStatusRequest;
import com.sparta.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ApiResponse<Void> createDelivery(
            @RequestBody CreateDeliveryRequest request
    ) {
        return deliveryService.createDelivery(request);
    }

    // 배송 단건 조회
    @GetMapping("/{deliveryId}")
    public ApiResponse<GetDeliveryResponse> getDelivery(
            @PathVariable UUID deliveryId
    ){
        return deliveryService.getDelivery(deliveryId);
    }

    // 배송 경로 배송 담당자 수정
    @PatchMapping("/{deliveryRecordId}/change")
    public ApiResponse<Void> changeDeliverer(
            @PathVariable UUID deliveryRecordId,
            @RequestBody ChangeDelivererRequest request
    ){
        return deliveryService.changeDeliverer(deliveryRecordId, request);
    }

    // 배송 상태 업데이트 요청
    @PatchMapping("/{deliveryRecordId}/status")
    public ApiResponse<Void> startDelivery(
            @PathVariable UUID deliveryRecordId,
            @RequestBody UpdateDeliveryStatusRequest request
            ) {
        return deliveryService.updateDeliveryStatus(deliveryRecordId, request);
    }

    // 배송 취소 요청
    @PatchMapping("/{deliveryId}/cancle")
    public ApiResponse<Void> cancleDelivery(
            @PathVariable UUID deliveryId
    ) {
        return deliveryService.cancleDelivery(deliveryId);
    }

}
