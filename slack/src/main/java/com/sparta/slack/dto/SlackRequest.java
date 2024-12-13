package com.sparta.slack.dto;

import com.sparta.order.dto.OrderItemRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record SlackRequest(
    @NotEmpty(message = "Channel cannot be empty")
    @Size(max = 50, message = "Channel name must not exceed 50 characters")
    String channel,  // 슬랙 채널

    @NotEmpty(message = "Text cannot be empty")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    String text,  // 메시지 내용

    String orderSupplierId,  // 공급자 ID
    String orderReceiverId,  // 수신자 ID
    String orderHubId,  // 허브 ID
    List<OrderItemRequest> orderItems,  // 주문 아이템 목록
    LocalDateTime expectedDeliveryDate,  // 예상 배송 날짜
    String orderNote,  // 주문 노트
    String requestDetails  // 요청 세부사항
) {
    // getChannel() 메서드 정의
    public String getChannel() {
        return this.channel;
    }

    // 각 필드에 대한 getter 메서드 추가
    public String getOrderSupplierId() {
        return this.orderSupplierId;
    }

    public String getOrderReceiverId() {
        return this.orderReceiverId;
    }

    public String getOrderHubId() {
        return this.orderHubId;
    }

    public List<OrderItemRequest> getOrderItems() {
        return this.orderItems;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return this.expectedDeliveryDate;
    }

    public String getOrderNote() {
        return this.orderNote;
    }

    public String getRequestDetails() {
        return this.requestDetails;
    }
}
