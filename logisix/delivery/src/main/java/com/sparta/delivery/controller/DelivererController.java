package com.sparta.delivery.controller;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.RegisterDelivererRequest;
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
}
