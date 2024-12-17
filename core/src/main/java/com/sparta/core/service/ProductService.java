package com.sparta.core.service;

import com.sparta.core.dto.ProductRequest;
import com.sparta.core.dto.ProductResponse;
import com.sparta.core.entity.Product;
import com.sparta.core.exception.LogisixException;
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

  public void createProduct(ProductRequest productRequest) {
    Optional<Product> productOptional = Optional.ofNullable(
        productRepository.findByProductName(productRequest.productName()));

    if (productOptional.isPresent()) {
      throw new LogisixException(ErrorCode.DUPLICATE_VALUE);
    }

    Product product = new Product(productRequest);
    productRepository.save(product);
  }

  public ProductResponse getProduct(UUID productId) {
    Optional<Product> productOptional = productRepository.findById(productId);

    if (productOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    Product product = productOptional.get();
    return ProductResponse.from(product);
  }

  public Page<ProductResponse> getProducts(int size, String keyword, Direction direction,
      Integer page) {
    Pageable pageable = PageRequest.of(page, size, direction);
    return productRepository.findByProductNameContaining(keyword, pageable)
        .map(ProductResponse::from);
  }

  @Transactional
  public void updateProduct(UUID productId, ProductRequest productRequest) {
    Optional<Product> productOptional = productRepository.findById(productId);

    if (productOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }

    Product fetchedProduct = productOptional.get();
    Product product = new Product(productRequest);

    fetchedProduct.update(product);
  }

  @Transactional
  public void deleteProduct(UUID productId) {
    Optional<Product> productOptional = productRepository.findById(productId);

    if (productOptional.isEmpty()) {
      throw new LogisixException(ErrorCode.VALUE_NOT_FOUND);
    }
  }
}
