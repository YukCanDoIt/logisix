package com.sparta.core.dto;

import com.sparta.core.entity.Product;
import java.util.UUID;

public record ProductResponse(
    String productName,
    UUID companyId,
    Long quantity
) {

  public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.getProductName(),
        product.getCompanyId(),
        product.getQuantity()
    );
  }

}
