package com.sparta.delivery.entity;

public enum DeliveryStatusEnum {

    HUB_WAIT, // 출발 허브 대기
    HUB_MOVE, // 허브-허브 간 배송 중
    HUB_ARRIVED, // 마지막 허브 도착
    IN_DELIVERY,  // 허브-업체 배송 중
    DONE, // 배송 완료
    CANCELED // 배송 취소
}
