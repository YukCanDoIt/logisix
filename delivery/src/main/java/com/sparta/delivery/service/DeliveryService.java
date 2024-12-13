package com.sparta.delivery.service;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.*;
import com.sparta.delivery.entity.*;
import com.sparta.delivery.repository.DeliverersJpaRepository;
import com.sparta.delivery.util.Point;
import com.sparta.delivery.repository.DeliveriesJpaRepository;
import com.sparta.delivery.repository.DeliveryRecordsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final PathService pathService;
    private final KakaoMapService kakaoMapService;
    private final DeliverersJpaRepository deliverersJpaRepository;
    private final DeliveriesJpaRepository deliveryJpaRepository;
    private final DeliveryRecordsJpaRepository deliveryRecordsJpaRepository;

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);


    // 배송 생성
    public ApiResponse<Void> createDelivery(CreateDeliveryRequest request) {

        try {
            // 사용자 유효성 체크

            Delivery delivery = createDeliveryEntity(request);

            // Redis에서 경로 데이터 가져오기
            List<HubRoute> hubRoutes = pathService.getHubRoutes();

            // 없을 경우 DB에서 가져오기

            // 최단 경로 생성
            List<DeliveryRecord> deliveryRecordList = createDeliveryRecords(request, hubRoutes, delivery);

            // 마지막 허브에서 업체까지의 경로 추가
            addFinalDeliveryRecord(deliveryRecordList, delivery);

            // 배송 시한 업데이트
            updateDispatchDeadline(delivery, deliveryRecordList, request);

            // 배송 데이터 저장
            saveDelivery(delivery, deliveryRecordList);

            // 슬랙 메시지 발송

            return new ApiResponse<>(200, "배송 생성 성공", null);
        } catch (Exception e) {
            logger.error("createDelivery failed: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "배송 생성 실패", null);
        }
    }

    private Delivery createDeliveryEntity(CreateDeliveryRequest request) {
        return Delivery.create(
                request.orderId(),
                request.sourceHubId(),
                request.destinationId(),
                request.companyAddress(),
                request.recipient(),
                request.recipientSlackAccount()
        );
    }

    private List<DeliveryRecord> createDeliveryRecords(CreateDeliveryRequest request, List<HubRoute> hubRoutes, Delivery delivery) {
        List<UUID> paths = pathService.findShortestPath(hubRoutes, request.sourceHubId(), request.destinationId());
        List<DeliveryRecord> deliveryRecordList = new ArrayList<>();

        for (int i = 0; i < paths.size() - 1; i++) {
            UUID departureHubId = paths.get(i);
            UUID arrivalHubId = paths.get(i + 1);

            HubRoute hubRoute = pathService.findHubRoute(hubRoutes, departureHubId, arrivalHubId);

            DeliveryRecord deliveryRecord = DeliveryRecord.create(
                    departureHubId,
                    arrivalHubId,
                    i + 1,
                    hubRoute.estimateTime(),
                    BigDecimal.valueOf(hubRoute.estimatedDistance()),
                    delivery
            );
            deliveryRecordList.add(deliveryRecord);
        }
        return deliveryRecordList;
    }

    private void addFinalDeliveryRecord(List<DeliveryRecord> deliveryRecordList, Delivery delivery) {
        Point lastHubLocation = new Point(BigDecimal.valueOf(126.977969), BigDecimal.valueOf(37.566535));
        Point companyLocation = new Point(BigDecimal.valueOf(127.1058342), BigDecimal.valueOf(37.359708));

        kakaoMapService.getRouteAsync(lastHubLocation, companyLocation)
                .thenAccept(response -> {
                    if (response != null && response.routes().length > 0) {
                        BigDecimal distance = response.routes()[0].summary().distance();
                        BigDecimal duration = response.routes()[0].summary().duration();
                        DeliveryRecord finalRecord = DeliveryRecord.create(
                                deliveryRecordList.get(deliveryRecordList.size() - 1).getArrival(),
                                delivery.getCompanyId(),
                                deliveryRecordList.size() + 1,
                                Duration.ofMillis(duration.longValue()),
                                distance,
                                delivery
                        );
                        deliveryRecordList.add(finalRecord);
                    } else {
                        logger.warn("유효한 경로가 없음");
                    }
                }).exceptionally(e -> {
                    logger.error("addFinalDeliveryRecord failed: {}", e.getMessage(), e);
                    return null;
                });
    }

    private void updateDispatchDeadline(Delivery delivery, List<DeliveryRecord> deliveryRecordList, CreateDeliveryRequest request) {
        Duration totalEstimatedTime = deliveryRecordList.stream()
                .map(DeliveryRecord::getEstimatedTime)
                .reduce(Duration.ZERO, Duration::plus);

        LocalDateTime dispatchDeadline = request.deliverDate().minus(totalEstimatedTime);

        delivery.setDispatchDeadline(dispatchDeadline);
    }

    private void saveDelivery(Delivery delivery, List<DeliveryRecord> deliveryRecordList) {
        delivery.setTotalSequence(deliveryRecordList.size());
        delivery.setCurrentSeq(0);
        deliveryJpaRepository.save(delivery);
        deliveryRecordsJpaRepository.saveAll(deliveryRecordList);
    }

    // 배송 단건 조회
    public ApiResponse<GetDeliveryResponse> getDelivery(UUID deliveryId) {
        // 사용자 권한 및 유효성 체크

        Optional<Delivery> delivery = deliveryJpaRepository.findByDeliveryId(deliveryId);
        if(delivery.isEmpty()) {
            return new ApiResponse<>(400, "해당하는 배송 정보가 없습니다", null);
        }

        List<DeliveryRecord> deliveryRecordList = deliveryRecordsJpaRepository.findAllByDelivery_DeliveryId(deliveryId);

        GetDeliveryResponse response = GetDeliveryResponse.create(delivery.get(), deliveryRecordList);

        return new ApiResponse<>(200, "배송 정보 조회 성공", response);
    }

    // 배송 경로 배송 담당자 수정
    public ApiResponse<Void> changeDeliverer(UUID deliveryRecordId, ChangeDelivererRequest request) {
        // 사용자 권한 및 유효성 체크

        try {
            // 배송 경로 정보 확인
            DeliveryRecord existRecord = deliveryRecordsJpaRepository.findById(deliveryRecordId)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 배송 경로 정보가 없습니다"));

            // 배송 담당자 정보 확인
            Deliverer existDeliverer = deliverersJpaRepository.findById(request.delivererId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 배송 담당자 정보가 없습니다"));

            // 배송 담당자 상태 확인
            if (existDeliverer.getStatus() != DelivererStatusEnum.WAIT) {
                return new ApiResponse<>(400, "배송중인 배송 담당자로 변경할 수 없습니다", null);
            }

            // 배송 경로 상태 확인
            if (existRecord.getDeliverer() == null) {
                return new ApiResponse<>(400, "아직 배송 담당자가 배정되지 않은 경로입니다", null);
            } else if (existRecord.getStatus() == DeliveryRecordsStatusEnum.COMPLETED) {
                return new ApiResponse<>(400, "이미 완료된 배송 경로입니다", null);
            }

            // 배송 담당자 변경
            existRecord.changeDeliverer(existDeliverer);
            deliveryRecordsJpaRepository.save(existRecord);

            return new ApiResponse<>(200, "배송 담당자 변경 성공", null);

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Unexpected error while changing deliverer: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "배송 담당자 변경 중 에러가 발생했습니다", null);
        }

    }
}
