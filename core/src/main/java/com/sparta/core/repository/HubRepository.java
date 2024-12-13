package com.sparta.core.repository;

import com.sparta.core.entity.Hub;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public interface HubRepository extends JpaRepository<Hub, UUID> {

  Page<Hub> findByHubNameContaining(String keyword, Pageable pageable);

  Hub findByHubName(String hubName);
}