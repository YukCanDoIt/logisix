package com.sparta.core.service;

import com.sparta.core.dto.HubResponse;
import com.sparta.core.dto.HubRouteRequest;
import com.sparta.core.dto.HubRouteResponse;
import com.sparta.core.dto.KakaoMapResponse;
import com.sparta.core.entity.Hub;
import com.sparta.core.entity.HubRoute;
import com.sparta.core.exception.ErrorCode;
import com.sparta.core.exception.LogisixException;
import com.sparta.core.repository.HubRepository;
import com.sparta.core.repository.HubRouteRedisRepository;
import com.sparta.core.repository.HubRouteRepository;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HubRouteService {

  private final KakaoMapService kakaoMapService;
  private final HubRepository hubRepository;
  private final HubRouteRepository hubRouteRepository;
  private final HubRouteRedisRepository hubRouteRedisRepository;

  public void createHubRoutes() {
    List<Hub> hubList = hubRepository.findAll();

    List<String> hubPairs = generateAllPairs(hubList);
    List<HubRoute> hubRouteList = new ArrayList<>();

    for (String hubPair : hubPairs) {
      String[] result = hubPair.split("\\+");
      String hubName1 = result[0];
      String hubName2 = result[1];

      Optional<Hub> hubOptional1 = hubList.stream()
          .filter(hub -> hub.getHubName().equals(hubName1))
          .findFirst();

      Optional<Hub> hubOptional2 = hubList.stream()
          .filter(hub -> hub.getHubName().equals(hubName2))
          .findFirst();

      if (hubOptional1.isEmpty() || hubOptional2.isEmpty()) {
        System.out.println(hubName1 + " + " + hubName2);
        throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
      }

      Hub hub1 = hubOptional1.get();
      Hub hub2 = hubOptional2.get();

      BigDecimal originLongitude = hub1.getLongitude();
      BigDecimal originLatitude = hub1.getLatitude();
      BigDecimal destinationLongitude = hub2.getLongitude();
      BigDecimal destinationLatitude = hub2.getLatitude();

      ResponseEntity<KakaoMapResponse> response = kakaoMapService.getDirections(
          originLongitude, originLatitude, destinationLongitude, destinationLatitude
      );
      KakaoMapResponse kakaoResponse = response.getBody();
      if (kakaoResponse != null && kakaoResponse.routes() != null
          && kakaoResponse.routes().length > 0) {
        KakaoMapResponse.Route route = kakaoResponse.routes()[0];

        HubRouteRequest hubRouteRequest = new HubRouteRequest(hub1.getHubId(), hub2.getHubId(),
            route.summary().distance(), route.summary().duration());
        HubRoute hubRoute = new HubRoute(hubRouteRequest);

        hubRouteList.add(hubRoute);
        hubRouteRepository.save(hubRoute);
      }

    }
    hubRouteRedisRepository.saveAll(hubRouteList);
  }

  /* 허브 매핑 정보에 따른 출발-도착 허브 리스트 GET 메서드*/
  public List<String> generateAllPairs(List<Hub> hubs) {
    Map<String, List<String>> hubConnections = new HashMap<>();

    /* Hub To Hub 연결 정보 */
    hubConnections.put("경기남부 센터",
        Arrays.asList("경기북부 센터", "서울특별시 센터", "인천광역시 센터", "강원특별자치도 센터", "경상북도 센터",
            "대전광역시 센터", "대구광역시 센터"));
    hubConnections.put("대전광역시 센터",
        Arrays.asList("충청남도 센터", "충청북도 센터", "세종특별자치시 센터", "전북특별자치도 센터", "광주광역시 센터", "전라남도 센터",
            "경기남부 센터",
            "대구광역시 센터"));
    hubConnections.put("대구광역시 센터",
        Arrays.asList("경상북도 센터", "경상남도 센터", "부산광역시 센터", "울산광역시 센터", "경기남부 센터", "대전광역시 센터"));
    hubConnections.put("경상북도 센터", Arrays.asList("경기남부 센터", "대구광역시 센터"));

    Set<String> uniqueConnections = new LinkedHashSet<>();

    for (Map.Entry<String, List<String>> entry : hubConnections.entrySet()) {
      String hub1 = entry.getKey();
      for (String hub2 : entry.getValue()) {
        String connection1 = hub1 + "+" + hub2;
        String connection2 = hub2 + "+" + hub1;

        uniqueConnections.add(connection1);
        uniqueConnections.add(connection2);
      }
    }

    return new ArrayList<>(uniqueConnections);
  }

  public HubRouteResponse getHubRoute(UUID arrivalHubId, UUID departureHubId) {
    HubRoute hubRoute = hubRouteRepository.findByArrivalHubIdAndDepartureHubId(arrivalHubId,
        departureHubId);

    if (hubRoute == null) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    return HubRouteResponse.from(hubRoute);
  }

  public List<HubRouteResponse> getHubRoutes() {
    return hubRouteRepository.findAll().stream()
        .map(HubRouteResponse::from).toList();
  }
}
