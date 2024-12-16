package com.sparta.core.service;

import com.sparta.core.dto.HubRequest;
import com.sparta.core.dto.HubResponse;
import com.sparta.core.entity.Hub;
import com.sparta.core.exception.LogisixException;
import com.sparta.core.exception.ErrorCode;
import com.sparta.core.repository.HubRepository;
import java.util.Optional;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

@Service
public class HubService {

  private final HubRouteService hubRouteService;
  private final HubRepository hubRepository;
  private final GeometryFactory geometryFactory;

  public HubService(HubRouteService hubRouteService, HubRepository hubRepository) {
    this.hubRouteService = hubRouteService;
    this.hubRepository = hubRepository;
    this.geometryFactory = new GeometryFactory();
  }


  public void createHub(HubRequest hubRequest) {
    Optional<Hub> hubOptional = Optional.ofNullable(
        hubRepository.findByHubName(hubRequest.hubName()));

    if (hubOptional.isPresent()) {
      throw new LogisixException(ErrorCode.DUPLICATE_VALUE);
    }

    Coordinate coordinate = new Coordinate(hubRequest.longitude().doubleValue(),
        hubRequest.latitude().doubleValue());
    Hub hub = new Hub(hubRequest, geometryFactory.createPoint(coordinate));
    hubRepository.save(hub);
  }

  public HubResponse getHub(UUID hubId) {
    Optional<Hub> hubOptional = hubRepository.findById(hubId);

    if (hubOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    Hub hub = hubOptional.get();
    return HubResponse.from(hub);
  }

  public Page<HubResponse> getHubs(int size, String keyword, Direction direction, String sortBy,
      Integer page
  ) {
    Pageable pageable = PageRequest.of(page, size, direction, sortBy);
    return hubRepository.findByHubNameContaining(keyword, pageable)
        .map(HubResponse::from);
  }

  @Transactional
  public void updateHub(UUID hubId, HubRequest hubRequest) {
    Optional<Hub> hubOptional = hubRepository.findById(hubId);

    if (hubOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    Hub fetchedHub = hubOptional.get();

    Coordinate coordinate = new Coordinate(hubRequest.longitude().doubleValue(),
        hubRequest.latitude().doubleValue());
    Hub hub = new Hub(hubRequest, geometryFactory.createPoint(coordinate));

    fetchedHub.update(hub);
    hubRouteService.createHubRoutes();
  }

  @DeleteMapping
  public void deleteHub(UUID hubId) {
    Optional<Hub> hubOptional = hubRepository.findById(hubId);

    if (hubOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    // TODO: Soft Delete 적용 필요
  }
}
