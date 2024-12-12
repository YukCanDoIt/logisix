package com.sparta.delivery.service;

import com.sparta.delivery.common.ApiResponse;
import com.sparta.delivery.dto.CreateDeliveryRequest;
import com.sparta.delivery.dto.HubRoute;
import com.sparta.delivery.entity.Point;
import com.sparta.delivery.dto.KakaoRouteResponse;
import com.sparta.delivery.entity.Deliveries;
import com.sparta.delivery.entity.DeliveryRecords;
import com.sparta.delivery.repository.DeliveriesJpaRepository;
import com.sparta.delivery.repository.DeliveryRecordsJpaRepository;
import lombok.RequiredArgsConstructor;
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

    // 배송 생성
    public ApiResponse createDelivery(CreateDeliveryRequest request) {
        // 사용자 권한 및 유효성 체크, 수신인 조회
        String recipient = "tempRecipient";
        String recipientSlackAccount = "tempRecipientSlackAccount";

        // 업체 조회
        String companyAddress = "tempCompanyAdress";
        Point companyLocation = new Point(BigDecimal.valueOf(127.1058342), BigDecimal.valueOf(37.359708));
        // 업체가 소속된 허브를 종착 허브로 설정

        // 배송 생성
        Deliveries delivery = Deliveries.create(
                request.orderId(),
                request.sourceHubId(),
                request.destinationId(),
                companyAddress,
                recipient,
                recipientSlackAccount
        );
        // Redis에서 허브 경로 데이터 가져오기
        List<HubRoute> hubRoutes = pathService.getHubRoutes();

//        if(hubRoutes == null) {
//            // 경로 데이터 없을 경우 DB에서 조회 필요
//        }
        // 배송 상세 경로 생성
        List<UUID> paths = pathService.findShortestPath(hubRoutes, request.sourceHubId(), request.destinationId());
        List<DeliveryRecords> deliveryRecordsList = new ArrayList<>();
        for (int i = 0; i < paths.size() - 1; i ++) {
            UUID departureHubId = paths.get(i);
            UUID arrivalHubId = paths.get(i + 1);

            HubRoute hubRoute = pathService.findHubRoute(hubRoutes, departureHubId, arrivalHubId);

            DeliveryRecords deliverRecord = DeliveryRecords.create(
                    departureHubId,
                    arrivalHubId,
                    i + 1,
                    hubRoute.estimateTime(),
                    BigDecimal.valueOf(hubRoute.estimatedDistance()),
                    delivery
            );
            deliveryRecordsList.add(deliverRecord);
        }
        // 마지막 허브 위치 조회
        Point lastHubLocation = new Point(BigDecimal.valueOf(126.977969), BigDecimal.valueOf(37.566535));

        // 업체까지의 배송 경로 레코드 생성
        KakaoRouteResponse response = kakaoMapService.getRoute(lastHubLocation, companyLocation);
        if(response != null && response.routes().length >0) {
            BigDecimal distance = response.routes()[0].summary().distance();
            BigDecimal duration = response.routes()[0].summary().duration();
            DeliveryRecords finalRecord = DeliveryRecords.create(
                    paths.get(paths.size() - 1),
                    request.destinationId(),
                    deliveryRecordsList.size() + 1,
                    Duration.ofMillis(duration.longValue()),
                    distance,
                    delivery
            );
            deliveryRecordsList.add(finalRecord);
        }

        // 시퀀스 업데이트
        Deliveries updatedDelivery = Deliveries.updateSequence(delivery, 0, deliveryRecordsList.size());

        // 배송 저장
        deliveryJpaRepository.save(updatedDelivery);

        // 배송 상세 경로 저장
        deliveryRecordsJpaRepository.saveAll(deliveryRecordsList);

        // 슬랙 메시지 전송

        // 반환
        return new ApiResponse<>(200, "배송 생성 성공", null);
    }


}
