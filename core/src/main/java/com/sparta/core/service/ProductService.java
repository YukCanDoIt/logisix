package com.sparta.core.service;

import com.sparta.core.dto.ProductRequestDto;
import com.sparta.core.dto.ProductResponseDto;
import com.sparta.core.entity.Product;
import com.sparta.core.exception.ApiException;
import com.sparta.core.exception.ErrorCode;
import com.sparta.core.repository.ProductRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  public void createProduct(ProductRequestDto productRequestDto) {
    Optional<Product> productOptional = Optional.ofNullable(
        productRepository.findByProductName(productRequestDto.productName()));

    if (productOptional.isPresent()) {
      throw new ApiException(ErrorCode.DUPLICATE_VALUE);
    }

    Product product = new Product(productRequestDto.productName(), productRequestDto.companyId(),
        productRequestDto.quantity());
    productRepository.save(product);
  }

  public ProductResponseDto getProduct(UUID productId) {
    Optional<Product> productOptional = productRepository.findById(productId);

    if (productOptional.isEmpty()) {
      throw new IllegalArgumentException("Product not found");
    }

    Product product = productOptional.get();
    return new ProductResponseDto(product.getProductName(), product.getCompanyId(),
        product.getQuantity());
  }

  public Page<ProductResponseDto> getProducts(int size, String keyword, Direction direction,
      Integer page) {
    Pageable pageable = PageRequest.of(page, size, direction);
    return productRepository.findByProductNameContaining(keyword, pageable).map(
        product -> new ProductResponseDto(product.productName(), product.companyId(),
            product.quantity()));
  }

  @Transactional
  public void updateProduct(UUID productId, ProductRequestDto productRequestDto) {
    Optional<Product> productOptional = productRepository.findById(productId);

    if (productOptional.isEmpty()) {
      throw new IllegalArgumentException("Product not found");
    }

    Product fetchedProduct = productOptional.get();
    Product product = new Product(productRequestDto.productName(), productRequestDto.companyId(),
        productRequestDto.quantity());
    fetchedProduct.update(product);
  }

  @Transactional
  public void deleteProduct(UUID productId) {
    Optional<Product> productOptional = productRepository.findById(productId);

    if (productOptional.isEmpty()) {
      throw new IllegalArgumentException("Product not found");
    }
  }
}
