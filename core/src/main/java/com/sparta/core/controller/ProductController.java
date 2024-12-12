package com.sparta.core.controller;

import com.sparta.core.dto.CompanyRequestDto;
import com.sparta.core.dto.CompanyResponseDto;
import com.sparta.core.dto.ProductRequestDto;
import com.sparta.core.dto.ProductResponseDto;
import com.sparta.core.response.ApiResponse;
import com.sparta.core.service.ProductService;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping
  public ResponseEntity createProduct(@Valid @RequestBody ProductRequestDto productRequestDto) {
    productService.createProduct(productRequestDto);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @GetMapping("/{productId}")
  public ResponseEntity getProduct(@PathVariable UUID productId) {
    ProductResponseDto productResponseDto = productService.getProduct(productId);
    return ResponseEntity.ok(ApiResponse.success(productResponseDto));
  }

  @GetMapping
  public ResponseEntity getProducts(
      @RequestParam int size,
      @RequestParam String keyword,
      // @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") Direction direction,
      @RequestParam Integer page
  ) {
    Page<ProductResponseDto> productResponseDtoList = productService.getProducts(size, keyword,
        direction,
        page - 1);
    return ResponseEntity.ok(ApiResponse.success(productResponseDtoList));
  }

  @PutMapping("/{productId}")
  public ResponseEntity updateProduct(@PathVariable UUID productId,
      @Valid @RequestBody ProductRequestDto productRequestDto) {
    productService.updateProduct(productId, productRequestDto);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity deleteProduct(@PathVariable UUID productId) {
    productService.deleteProduct(productId);
    return ResponseEntity.ok(ApiResponse.success());
  }


}
