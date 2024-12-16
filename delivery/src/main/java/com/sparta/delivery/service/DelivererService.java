package com.sparta.delivery.service;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.GetDelivererResponse;
import com.sparta.delivery.dto.RegisterDelivererRequest;
import com.sparta.delivery.dto.UpdateDelivererRequest;
import com.sparta.delivery.entity.DelivererStatusEnum;
import com.sparta.delivery.entity.Deliverer;
import com.sparta.delivery.repository.DeliverersJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


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

        Deliverer deliverer = Deliverer.create(
                request.delivererId(),
                request.hubId(),
                request.type()
        );
        deliverersJpaRepository.save(deliverer);

        return new ApiResponse<>(200, "배송담당자 추가 완료", null);
    }

    // 배송 담당자 단건 조회
    @Transactional(readOnly = true)
    public ApiResponse<GetDelivererResponse> getDeliverer(Long delivererId) {
        Optional<Deliverer> optionalDeliverer = deliverersJpaRepository.findByDelivererId(delivererId);
        if (optionalDeliverer.isPresent()) {
            Deliverer deliverer = optionalDeliverer.get();
            return new ApiResponse<>(200, "배송 담당자 조회 성공", GetDelivererResponse.from(deliverer));
        } else {
            return new ApiResponse<>(400, "해당하는 배송 담당자가 없습니다", null);
        }
    }

    // 배송 담당자 정보 수정
    public ApiResponse<GetDelivererResponse> updateDeliverer(Long delivererId, UpdateDelivererRequest request) {
        Optional<Deliverer> optionalDeliverer = deliverersJpaRepository.findByDelivererId(delivererId);
        if (optionalDeliverer.isPresent()) {
            Deliverer deliverer = optionalDeliverer.get();
            deliverer.update(request.hubId(), request.type());
            deliverersJpaRepository.save(deliverer);
            return new ApiResponse<>(200, "배송 담당자 수정 완료", GetDelivererResponse.from(deliverer));
        } else {
            return new ApiResponse<>(400, "해당하는 배송 담당자가 없습니다", null);
        }
    }

    // 배송 담당자 정보 삭제
    public ApiResponse<Void> deleteDeliverer(Long delivererId) {
        Optional<Deliverer> optionalDeliverer = findDelivererById(delivererId);
        if (optionalDeliverer.isPresent()) {
            Deliverer deliverer = optionalDeliverer.get();
            if (deliverer.getStatus() == DelivererStatusEnum.MOVING) {
                return new ApiResponse<>(400, "배송 중인 배송 담당자는 삭제할 수 없습니다", null);
            }

            deliverer.deleteBase("temp_username");
            deliverersJpaRepository.save(deliverer);
            return new ApiResponse<>(200, "배송 담당자 삭제 완료", null);
        } else {
            return new ApiResponse<>(400, "해당하는 배송 담당자가 없습니다", null);
        }
    }

    // 배송 담당자 배정 - 허브-업체
    public Deliverer assignCompanyDeliverer(UUID departureHubId) {
        return deliverersJpaRepository.findCompanyDeliverersByHub(departureHubId).stream()
                .min(Comparator.comparing(deliverer -> deliverer.getDeliveryRecords().size()))
                .orElseThrow(() -> new IllegalArgumentException("허브-업체 배송에 배정 가능한 담당자가 없습니다."));
    }

    // 배송 담당자 배정 - 허브-허브
    public Deliverer assignHubDeliverer(Map<UUID, Double> distances) {
        List<Deliverer> hubDeliverers = deliverersJpaRepository.findHubDeliverers();
        return hubDeliverers.stream()
                .filter(deliverer -> deliverer.getStatus() != DelivererStatusEnum.MOVING) // 배송 중이지 않은 담당자 우선 배정
                .min(Comparator.comparing(deliverer -> distances.getOrDefault(deliverer.getHubId(), Double.MAX_VALUE)))
                .orElseThrow(() -> new IllegalArgumentException("허브-허브 배송에 배정 가능한 담당자가 없습니다."));
    }

    private Optional<Deliverer> findDelivererById(Long delivererId) {
        return deliverersJpaRepository.findByDelivererId(delivererId);
    }


}
