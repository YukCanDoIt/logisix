package com.sparta.core.service;

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
import java.util.List;
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
    // (1) hubTable에서 모든 hubId를 가져와서, 2개씩 조합한다.
    // (2) 조합된 hubId끼리 카카오 길찾기 API를 호출한다.
    // (3) 예상 시간, 예상 거리를 hubRoute 테이블에 저장한다.
    // (4) 래디스에 캐시 저장한다.

    List<Hub> hubList = hubRepository.findAll();
    List<List<Hub>> hubPairs = generateAllPairs(hubList);
    List<HubRoute> hubRouteList = new ArrayList<>();

    for (List<Hub> pair : hubPairs) {
      if (pair.size() == 2) {
        Hub hub1 = pair.get(0);
        Hub hub2 = pair.get(1);

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
    }
    hubRouteRedisRepository.saveAll(hubRouteList);
  }

  public List<List<Hub>> generateAllPairs(List<Hub> hubs) {
    List<List<Hub>> pairs = new ArrayList<>();

    for (int i = 0; i < hubs.size(); i++) {
      for (int j = i + 1; j < hubs.size(); j++) {
        List<Hub> pair = new ArrayList<>();
        pair.add(hubs.get(i));
        pair.add(hubs.get(j));
        pairs.add(pair);
      }
    }

    return pairs;
  }

  public HubRouteResponse getHubRoute(UUID arrivalHubId, UUID departureHubId) {
    HubRoute hubRoute = hubRouteRepository.findByArrivalHubIdAndDepartureHubId(arrivalHubId,
        departureHubId);

    if (hubRoute == null) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    return HubRouteResponse.from(hubRoute);
  }
}
