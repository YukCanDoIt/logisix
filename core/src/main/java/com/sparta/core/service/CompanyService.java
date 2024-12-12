package com.sparta.core.service;

import com.sparta.core.dto.CompanyRequestDto;
import com.sparta.core.dto.CompanyResponseDto;
import com.sparta.core.entity.Company;
import com.sparta.core.entity.Hub;
import com.sparta.core.exception.ApiException;
import com.sparta.core.exception.ErrorCode;
import com.sparta.core.repository.CompanyRepository;
import jakarta.validation.Valid;
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
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final GeometryFactory geometryFactory;

  public CompanyService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
    this.geometryFactory = new GeometryFactory();
  }

  public void createCompany(CompanyRequestDto companyRequestDto) {
    Optional<Company> optionalCompany = Optional.ofNullable(
        companyRepository.findByCompanyName(companyRequestDto.companyName()));

    if (optionalCompany.isPresent()) {
      throw new ApiException(ErrorCode.DUPLICATE_VALUE);
    }

    Coordinate coordinate = new Coordinate(companyRequestDto.longitude(),
        companyRequestDto.latitude());
    Company company = new Company(companyRequestDto.companyName(), companyRequestDto.address(),
        companyRequestDto.latitude(), companyRequestDto.longitude(), companyRequestDto.hubId(),
        companyRequestDto.companyType(),
        geometryFactory.createPoint(coordinate));

    companyRepository.save(company);
  }

  public CompanyResponseDto getCompany(UUID companyId) {
    Optional<Company> companyOptional = companyRepository.findById(companyId);

    if (companyOptional.isEmpty()) {
      throw new IllegalArgumentException("Company not found");
    }

    Company company = companyOptional.get();
    return new CompanyResponseDto(company.getCompanyName(), company.getAddress(),
        company.getType(), company.getLatitude(), company.getLongitude(),
        company.getHubId(), company.getCompanyManagerId());
  }

  public Page<CompanyResponseDto> getCompanies(int size, String keyword, Direction direction,
      Integer page) {
    Pageable pageable = PageRequest.of(page, size, direction);

    return companyRepository.findByCompanyNameContaining(keyword, pageable)
        .map(company -> new CompanyResponseDto(company.getCompanyName(), company.getAddress(),
            company.getType(), company.getLatitude(), company.getLongitude(),
            company.getHubId(), company.getCompanyManagerId()));
  }

  @Transactional
  public void updateCompany(UUID companyId, @Valid CompanyRequestDto companyRequestDto) {
    Optional<Company> companyOptional = companyRepository.findById(companyId);

    if (companyOptional.isEmpty()) {
      throw new IllegalArgumentException("Company not found");
    }

    Company fetchedCompany = companyOptional.get();

    Coordinate coordinate = new Coordinate(companyRequestDto.longitude(),
        companyRequestDto.latitude());
    Company company = new Company(companyRequestDto.companyName(), companyRequestDto.address(),
        companyRequestDto.latitude(),
        companyRequestDto.longitude(),
        companyRequestDto.hubId(),
        companyRequestDto.companyType(),
        geometryFactory.createPoint(coordinate)
    );

    fetchedCompany.update(company);
  }

  @DeleteMapping
  public void deleteCompany(UUID companyId) {
    Optional<Company> companyOptional = companyRepository.findById(companyId);

    if (companyOptional.isEmpty()) {
      throw new IllegalArgumentException("Company not found");
    }

    // TODO: Soft Delete 적용 필요
  }
}
