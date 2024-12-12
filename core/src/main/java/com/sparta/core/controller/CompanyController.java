package com.sparta.core.controller;

import com.sparta.core.dto.CompanyRequestDto;
import com.sparta.core.dto.CompanyResponseDto;
import com.sparta.core.dto.HubRequestDto;
import com.sparta.core.dto.HubResponseDto;
import com.sparta.core.entity.Company;
import com.sparta.core.response.ApiResponse;
import com.sparta.core.service.CompanyService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

  private final CompanyService companyService;

  @PostMapping
  public ResponseEntity createCompany(@Valid @RequestBody CompanyRequestDto companyRequestDto) {
    companyService.createCompany(companyRequestDto);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @GetMapping("/{companyId}")
  public ResponseEntity getHub(@PathVariable UUID companyId) {
    CompanyResponseDto companyResponseDto = companyService.getCompany(companyId);
    return ResponseEntity.ok(ApiResponse.success(companyResponseDto));
  }

  @GetMapping
  public ResponseEntity getCompanies(
      @RequestParam int size,
      @RequestParam String keyword,
      // @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") Direction direction,
      @RequestParam Integer page
  ) {
    Page<CompanyResponseDto> companyResponseDtoList = companyService.getCompanies(size, keyword,
        direction,
        page - 1);
    return ResponseEntity.ok(ApiResponse.success(companyResponseDtoList));
  }

  @PutMapping("/{companyId}")
  public ResponseEntity updateCompany(@PathVariable UUID companyId,
      @Valid @RequestBody CompanyRequestDto companyRequestDto) {
    companyService.updateCompany(companyId, companyRequestDto);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @DeleteMapping("/{companyId}")
  public ResponseEntity deleteCompany(@PathVariable UUID companyId) {
    companyService.deleteCompany(companyId);
    return ResponseEntity.ok(ApiResponse.success());
  }
}
