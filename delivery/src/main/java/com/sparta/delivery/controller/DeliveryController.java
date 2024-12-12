package com.sparta.delivery.controller;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.CreateDeliveryRequest;
import com.sparta.delivery.dto.GetDeliveryResponse;
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

}
