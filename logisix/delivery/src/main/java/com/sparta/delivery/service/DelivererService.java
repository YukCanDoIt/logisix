package com.sparta.delivery.service;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.GetDelivererResponse;
import com.sparta.delivery.dto.RegisterDelivererRequest;
import com.sparta.delivery.dto.UpdateDelivererRequest;
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

    // 배송 담당자 단건 조회
    public ApiResponse<GetDelivererResponse> getDeliverer(Long delivererId) {
        // 사용자 권한 및 유효성 체크

        Optional<Deliverers> deliverer = deliverersJpaRepository.findByDelivererId(delivererId);
        if (deliverer.isEmpty()) {
            return new ApiResponse<>(400, "해당하는 배송 담당자가 없습니다", null);
        }

        return new ApiResponse<>(200, "배송 담당자 조회 성공", GetDelivererResponse.from(deliverer.get()));

    }

    // 배송 담당자 정보 수정
    public ApiResponse<GetDelivererResponse> updateDeliverer(Long delivererId, UpdateDelivererRequest request) {
        // 사용자 권한 및 유효성 체크

        Optional<Deliverers> deliverer = deliverersJpaRepository.findByDelivererId(delivererId);
        if (deliverer.isEmpty()) {
            return new ApiResponse<>(400, "해당하는 배송 담당자가 없습니다", null);
        }
        Deliverers existDeliverer = deliverer.get();
        existDeliverer.update(request.hubId(), request.type());
        deliverersJpaRepository.save(existDeliverer);
        return new ApiResponse<>(200, "배송 담당자 수정 완료", GetDelivererResponse.from(existDeliverer));
    }
}
