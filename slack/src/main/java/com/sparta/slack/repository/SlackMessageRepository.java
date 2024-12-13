package com.sparta.slack.repository;

import com.sparta.slack.domain.SlackMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SlackMessageRepository extends JpaRepository<SlackMessage, UUID> {
  // JpaRepository에서 제공하는 기본 메서드 사용
}
