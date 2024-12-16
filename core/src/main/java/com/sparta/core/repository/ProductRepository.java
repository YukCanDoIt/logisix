package com.sparta.core.repository;

import com.sparta.core.entity.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  Product findByProductName(String productName);

  Page<Product> findByProductNameContaining(String keyword, Pageable pageable);
}
