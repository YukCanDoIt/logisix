package com.sparta.core.repository;

import com.sparta.core.entity.Company;
import com.sparta.core.entity.Hub;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.channels.FileChannel;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

  Company findByCompanyName(String companyName);

  Page<Company> findByCompanyNameContaining(String keyword, Pageable pageable);
}
