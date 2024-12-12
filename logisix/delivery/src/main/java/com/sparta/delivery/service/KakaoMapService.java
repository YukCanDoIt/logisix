package com.sparta.delivery.service;

import com.sparta.delivery.dto.KakaoRouteResponse;
import com.sparta.delivery.entity.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String KAKAO_API_KEY;
    private static final String DIRECTIONS_URL = "https://apis-navi.kakaomobility.com/v1/directions";

    public KakaoRouteResponse getRoute(Point origin, Point destination) {
        // 요청 URL 생성
        String url = UriComponentsBuilder.fromUriString(DIRECTIONS_URL)
                .queryParam("origin", origin.getLongitude() + "," + origin.getLatitude())
                .queryParam("destination", destination.getLongitude() + "," + destination.getLatitude())
                .queryParam("priority", "SHORTEST")
                .toUriString();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_API_KEY);

        // API 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KakaoRouteResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                KakaoRouteResponse.class
        );

        return response.getBody();
    }

}
