package com.sparta.slack.repository;

import com.sparta.slack.domain.SlackMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@Rollback(false) // 롤백 방지 (DB에 데이터 저장 확인)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 PostgreSQL 사용
class SlackMessageRepositoryTest {

  @Autowired
  private SlackMessageRepository slackMessageRepository;

  @Test
  void saveSlackMessage_Success() {
    // Given
    LocalDateTime now = LocalDateTime.now();

    SlackMessage message = SlackMessage.builder()
        .channel("YukCanDoIt")
        .message("This is a test message for Slack.")
        .timestamp(now)
        .createdAt(now)
        .build();

    // When
    SlackMessage savedMessage = slackMessageRepository.save(message);

    // Then
    assertThat(savedMessage).isNotNull();
    assertThat(savedMessage.getId()).isNotNull(); // 자동 생성된 ID 확인
    assertThat(savedMessage.getChannel()).isEqualTo("test-channel");
    assertThat(savedMessage.getMessage()).isEqualTo("This is a test message for Slack.");
    assertThat(savedMessage.getTimestamp()).isCloseTo(now, within(1, ChronoUnit.SECONDS));
    assertThat(savedMessage.getCreatedAt()).isCloseTo(now, within(1, ChronoUnit.SECONDS));
  }
}
