package com.sparta.delivery.service;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.CreateDeliveryRequest;
import com.sparta.delivery.dto.HubRoute;
import com.sparta.delivery.entity.Deliveries;
import com.sparta.delivery.entity.DeliveryRecords;
import com.sparta.delivery.util.Point;
import com.sparta.delivery.repository.DeliveriesJpaRepository;
import com.sparta.delivery.repository.DeliveryRecordsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final PathService pathService;
    private final KakaoMapService kakaoMapService;
    private final DeliveriesJpaRepository deliveryJpaRepository;
    private final DeliveryRecordsJpaRepository deliveryRecordsJpaRepository;

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);


    // 배송 생성
    public ApiResponse<Void> createDelivery(CreateDeliveryRequest request) {

        try {
            // 사용자 유효성 체크

            Deliveries delivery = createDeliveryEntity(request);

            // Redis에서 경로 데이터 가져오기
            List<HubRoute> hubRoutes = pathService.getHubRoutes();

            // 없을 경우 DB에서 가져오기

            // 최단 경로 생성
            List<DeliveryRecords> deliveryRecordsList = createDeliveryRecords(request, hubRoutes, delivery);

            // 마지막 허브에서 업체까지의 경로 추가
            addFinalDeliveryRecord(deliveryRecordsList, delivery);

            // 배송 데이터 저장
            saveDelivery(delivery, deliveryRecordsList);

            return new ApiResponse<>(200, "배송 생성 성공", null);
        } catch (Exception e) {
            logger.error("Error occurred while creating delivery: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "배송 생성 실패", null);
        }
    }

    private Deliveries createDeliveryEntity(CreateDeliveryRequest request) {
        return Deliveries.create(
                request.orderId(),
                request.sourceHubId(),
                request.destinationId(),
                "tempCompanyAddress",
                "tempRecipient",
                "tempRecipientSlackAccount"
        );
    }

    private List<DeliveryRecords> createDeliveryRecords(CreateDeliveryRequest request, List<HubRoute> hubRoutes, Deliveries delivery) {
        List<UUID> paths = pathService.findShortestPath(hubRoutes, request.sourceHubId(), request.destinationId());
        List<DeliveryRecords> deliveryRecordsList = new ArrayList<>();

        for (int i = 0; i < paths.size() - 1; i++) {
            UUID departureHubId = paths.get(i);
            UUID arrivalHubId = paths.get(i + 1);

            HubRoute hubRoute = pathService.findHubRoute(hubRoutes, departureHubId, arrivalHubId);

            DeliveryRecords deliveryRecord = DeliveryRecords.create(
                    departureHubId,
                    arrivalHubId,
                    i + 1,
                    hubRoute.estimateTime(),
                    BigDecimal.valueOf(hubRoute.estimatedDistance()),
                    delivery
            );
            deliveryRecordsList.add(deliveryRecord);
        }
        return deliveryRecordsList;
    }

    private void addFinalDeliveryRecord(List<DeliveryRecords> deliveryRecordsList, Deliveries delivery) {
        Point lastHubLocation = new Point(BigDecimal.valueOf(126.977969), BigDecimal.valueOf(37.566535));
        Point companyLocation = new Point(BigDecimal.valueOf(127.1058342), BigDecimal.valueOf(37.359708));

        kakaoMapService.getRouteAsync(lastHubLocation, companyLocation)
                .thenAccept(response -> {
                    if (response != null && response.routes().length > 0) {
                        BigDecimal distance = response.routes()[0].summary().distance();
                        BigDecimal duration = response.routes()[0].summary().duration();
                        DeliveryRecords finalRecord = DeliveryRecords.create(
                                deliveryRecordsList.get(deliveryRecordsList.size() - 1).getArrival(),
                                delivery.getCompanyId(),
                                deliveryRecordsList.size() + 1,
                                Duration.ofMillis(duration.longValue()),
                                distance,
                                delivery
                        );
                        deliveryRecordsList.add(finalRecord);
                    } else {
                        logger.warn("유효한 경로가 없음");
                    }
                }).exceptionally(e -> {
                    logger.error("addFinalDeliveryRecord failed: {}", e.getMessage(), e);
                    return null;
                });
    }

    private void saveDelivery(Deliveries delivery, List<DeliveryRecords> deliveryRecordsList) {
        delivery.setTotalSequence(deliveryRecordsList.size());
        delivery.setCurrentSeq(0);
        deliveryJpaRepository.save(delivery);
        deliveryRecordsJpaRepository.saveAll(deliveryRecordsList);
    }

}
