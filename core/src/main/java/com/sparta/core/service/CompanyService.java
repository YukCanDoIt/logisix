package com.sparta.core.service;

import com.sparta.core.dto.CompanyRequest;
import com.sparta.core.dto.CompanyResponse;
import com.sparta.core.entity.Company;
import com.sparta.core.exception.LogisixException;
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

  public void createCompany(CompanyRequest companyRequest) {
    Optional<Company> optionalCompany = Optional.ofNullable(
        companyRepository.findByCompanyName(companyRequest.companyName()));

    if (optionalCompany.isPresent()) {
      throw new LogisixException(ErrorCode.DUPLICATE_VALUE);
    }

    Coordinate coordinate = new Coordinate(companyRequest.longitude().doubleValue(),
        companyRequest.latitude().doubleValue());
    Company company = new Company(companyRequest, geometryFactory.createPoint(coordinate));
    companyRepository.save(company);
  }

  public CompanyResponse getCompany(UUID companyId) {
    Optional<Company> companyOptional = companyRepository.findById(companyId);

    if (companyOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    Company company = companyOptional.get();
    return CompanyResponse.from(company);
  }

  public Page<CompanyResponse> getCompanies(int size, String keyword, Direction direction,
      Integer page) {
    Pageable pageable = PageRequest.of(page, size, direction);
    return companyRepository.findByCompanyNameContaining(keyword, pageable)
        .map(CompanyResponse::from);
  }

  @Transactional
  public void updateCompany(UUID companyId, @Valid CompanyRequest companyRequest) {
    Optional<Company> companyOptional = companyRepository.findById(companyId);

    if (companyOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    Company fetchedCompany = companyOptional.get();

    Coordinate coordinate = new Coordinate(companyRequest.longitude().doubleValue(),
        companyRequest.latitude().doubleValue());
    Company company = new Company(companyRequest, geometryFactory.createPoint(coordinate)
    );

    fetchedCompany.update(company);
  }

  @DeleteMapping
  public void deleteCompany(UUID companyId) {
    Optional<Company> companyOptional = companyRepository.findById(companyId);

    if (companyOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    // TODO: Soft Delete 적용 필요
  }
}
