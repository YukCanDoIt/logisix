package com.sparta.delivery.service;

import com.sparta.delivery.dto.KakaoRouteResponse;
import com.sparta.delivery.util.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.CompletableFuture;


@Service
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Async
    public CompletableFuture<KakaoRouteResponse> getRouteAsync(Point origin, Point destination) {
        try {
            String url = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("apis-navi.kakaomobility.com")
                    .path("/v1/directions")
                    .queryParam("origin", origin.getLongitude() + "," + origin.getLatitude())
                    .queryParam("destination", destination.getLongitude() + "," + destination.getLatitude())
                    .queryParam("priority", "SHORTEST")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<KakaoRouteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoRouteResponse.class
            );

            return CompletableFuture.completedFuture(response.getBody());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
