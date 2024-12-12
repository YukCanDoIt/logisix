package com.sparta.delivery.service;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.GetDelivererResponse;
import com.sparta.delivery.dto.RegisterDelivererRequest;
import com.sparta.delivery.dto.UpdateDelivererRequest;
import com.sparta.delivery.entity.DelivererStatusEnum;
import com.sparta.delivery.entity.Deliverers;
import com.sparta.delivery.repository.DeliverersJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DelivererService {

    private final DeliverersJpaRepository deliverersJpaRepository;

    // 배송 담당자 추가
    public ApiResponse<Void> registerDeliverer(RegisterDelivererRequest request) {
        // 사용자 권한 및 유효성 체크

        // 중복 체크
        if (deliverersJpaRepository.findByDelivererId(request.delivererId()).isPresent()) {
            return new ApiResponse<>(400, "이미 등록된 배송 담당자입니다", null);
        }

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

        ApiResponse<Deliverers> response = findDelivererById(delivererId);
        if (response.data() == null) {
            return new ApiResponse<>(response.status(), response.message(), null);
        }

        return new ApiResponse<>(200, "배송 담당자 조회 성공", GetDelivererResponse.from(response.data()));
    }

    // 배송 담당자 정보 수정
    public ApiResponse<GetDelivererResponse> updateDeliverer(Long delivererId, UpdateDelivererRequest request) {
        // 사용자 권한 및 유효성 체크

        ApiResponse<Deliverers> response = findDelivererById(delivererId);
        if (response.data() == null) {
            return new ApiResponse<>(response.status(), response.message(), null);
        }

        Deliverers deliverer = response.data();
        deliverer.update(request.hubId(), request.type());
        deliverersJpaRepository.save(deliverer);
        return new ApiResponse<>(200, "배송 담당자 수정 완료", GetDelivererResponse.from(deliverer));
    }

    // 배송 담당자 정보 삭제
    public ApiResponse<Void> deleteDeliverer(Long delivererId) {
        // 사용자 권한 및 유효성 체크

        ApiResponse<Deliverers> response = findDelivererById(delivererId);
        if (response.data() == null) {
            return new ApiResponse<>(response.status(), response.message(), null);
        }

        Deliverers deliverer = response.data();
        // 배송 진행 중일 경우
        if (deliverer.getStatus() == DelivererStatusEnum.MOVING) {
            return new ApiResponse<>(400, "배송 중인 배송 담당자는 삭제할 수 없습니다", null);
        }

        deliverersJpaRepository.delete(deliverer);
        return new ApiResponse<>(200, "배송 담당자 삭제 완료", null);
    }

    // 배송 담당자 조회
    private ApiResponse<Deliverers> findDelivererById(Long delivererId) {
        return deliverersJpaRepository.findByDelivererId(delivererId)
                .map(deliverer -> new ApiResponse<>(200, "배송 담당자 조회 성공", deliverer))
                .orElseGet(() -> new ApiResponse<>(400, "해당하는 배송 담당자가 없습니다", null));
    }
}
