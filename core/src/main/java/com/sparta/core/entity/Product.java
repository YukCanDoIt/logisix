package com.sparta.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_products")
@NoArgsConstructor
@Getter
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID productId;

  @Column(name = "product_name", nullable = false, length = 100)
  private String productName;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "company_id")
  private UUID companyId;

  public Product(String productName, UUID companyId, Long quantity) {
    this.productName = productName;
    this.companyId = companyId;
    this.quantity = quantity;
  }

  public void update(Product product) {
    this.productName = product.getProductName();
    this.quantity = product.getQuantity();
    this.companyId = product.getCompanyId();
  }
}
