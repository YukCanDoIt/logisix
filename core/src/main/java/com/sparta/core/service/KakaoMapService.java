package com.sparta.core.service;

import com.sparta.core.dto.KakaoMapResponse;
import com.sparta.core.exception.ErrorCode;
import com.sparta.core.exception.LogisixException;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class KakaoMapService {

  private final RestTemplate restTemplate;

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  @Async
  public ResponseEntity<KakaoMapResponse> getDirections(BigDecimal originLongitude,
      BigDecimal originLatitude,
      BigDecimal destinationLongitude, BigDecimal destinationLatitude) {
    try {
      String url = UriComponentsBuilder.newInstance()
          .scheme("https")
          .host("apis-navi.kakaomobility.com")
          .path("/v1/directions")
          .queryParam("origin", originLongitude + "," + originLatitude)
          .queryParam("destination", destinationLongitude + "," + destinationLatitude)
          .build()
          .toUriString();

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "KakaoAK " + kakaoApiKey);

      return restTemplate.exchange(
          url,
          HttpMethod.GET,
          new HttpEntity<>(headers),
          KakaoMapResponse.class
      );

    } catch (Exception e) {
      throw new LogisixException(ErrorCode.API_CALL_FAILED);
    }
  }

}
