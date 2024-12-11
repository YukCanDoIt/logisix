package com.sparta.delivery.service;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.RegisterDelivererRequest;
import com.sparta.delivery.entity.Deliverers;
import com.sparta.delivery.repository.DeliverersJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DelivererService {

    private final DeliverersJpaRepository deliverersJpaRepository;


    // 배송 담당자 추가
    public ApiResponse<Void> registerDeliverer(RegisterDelivererRequest request) {
        // 사용자 권한 및 유효성 체크

        // 중복 체크
        Optional<Deliverers> existDeliverer = deliverersJpaRepository.findByDelivererId(request.delivererId());
        if (existDeliverer.isPresent()) {
            return new ApiResponse<>(400, "이미 등록된 배송 담당자입니다", null);
        }

        // 허브 유효성 체크

        // 배송 담당자 추가
        Deliverers deliverer = Deliverers.create(
                request.delivererId(),
                request.hubId(),
                request.type()
        );
        deliverersJpaRepository.save(deliverer);

        return new ApiResponse<>(200, "배송담당자 추가 완료", null);

    }
}
