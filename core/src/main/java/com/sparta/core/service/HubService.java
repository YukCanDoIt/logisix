package com.sparta.core.service;

import com.sparta.core.dto.HubRequestDto;
import com.sparta.core.dto.HubResponseDto;
import com.sparta.core.entity.Hub;
import com.sparta.core.exception.ApiException;
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

  private final HubRepository hubRepository;
  private final GeometryFactory geometryFactory;

  public HubService(HubRepository hubRepository) {
    this.hubRepository = hubRepository;
    this.geometryFactory = new GeometryFactory();
  }


  public void createHub(HubRequestDto hubRequestDto) {
    Optional<Hub> hubOptional = Optional.ofNullable(
        hubRepository.findByHubName(hubRequestDto.hubName()));

    if (hubOptional.isPresent()) {
      throw new ApiException(ErrorCode.DUPLICATE_VALUE);
    }

    Coordinate coordinate = new Coordinate(hubRequestDto.longitude(), hubRequestDto.latitude());
    Hub hub = new Hub(hubRequestDto.hubName(), hubRequestDto.address(), hubRequestDto.latitude(),
        hubRequestDto.longitude(),
        geometryFactory.createPoint(coordinate));

    hubRepository.save(hub);
  }

  public HubResponseDto getHub(UUID hubId) {
    Optional<Hub> hubOptional = hubRepository.findById(hubId);

    if (hubOptional.isEmpty()) {
      throw new IllegalArgumentException("Hub not found");
    }

    Hub hub = hubOptional.get();
    return new HubResponseDto(hub.getHubName(), hub.getAddress(), hub.getLatitude(),
        hub.getLatitude(), hub.getHubManagerId());
  }

  public Page<HubResponseDto> getHubs(int size, String keyword, Direction direction,
      Integer page
  ) {
    Pageable pageable = PageRequest.of(page, size);

    return hubRepository.findByHubNameContaining(keyword, pageable)
        .map(hub -> new HubResponseDto(hub.getHubName(), hub.getAddress(), hub.getLongitude(),
            hub.getLatitude(), hub.getHubManagerId()));
  }

  @Transactional
  public void updateHub(UUID hubId, HubRequestDto hubRequestDto) {
    Optional<Hub> hubOptional = hubRepository.findById(hubId);

    if (hubOptional.isEmpty()) {
      throw new IllegalArgumentException("Hub not found");
    }

    Hub fetchedHub = hubOptional.get();

    Coordinate coordinate = new Coordinate(hubRequestDto.longitude(), hubRequestDto.latitude());
    Hub hub = new Hub(hubRequestDto.hubName(), hubRequestDto.address(), hubRequestDto.latitude(),
        hubRequestDto.longitude(),
        geometryFactory.createPoint(coordinate));

    fetchedHub.update(hub);
  }

  @DeleteMapping
  public void deleteHub(UUID hubId) {
    Optional<Hub> hubOptional = hubRepository.findById(hubId);

    if (hubOptional.isEmpty()) {
      throw new IllegalArgumentException("Hub not found");
    }

    // TODO: Soft Delete 적용 필요
  }
}
