package com.sparta.slack.repository;

import com.sparta.slack.domain.SlackMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.test.annotation.Rollback;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@Rollback(false) // 트랜잭션 롤백 방지
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 실제 DB 사용
class SlackMessageRepositoryTest {

  @Autowired
  private SlackMessageRepository slackMessageRepository;

  @Test
  void saveSlackMessage_Success() {
    // Given
    LocalDateTime now = LocalDateTime.now();

    SlackMessage message = SlackMessage.builder()
        .channel("TestChannel")
        .message("Test Slack Message")
        .timestamp(now)
        .createdAt(now)
        .build();

    // When
    SlackMessage savedMessage = slackMessageRepository.save(message);

    // Then
    assertThat(savedMessage).isNotNull();
    assertThat(savedMessage.getId()).isNotNull();  // ID 자동 생성 확인
    assertThat(savedMessage.getChannel()).isEqualTo("TestChannel"); // 채널 확인
    assertThat(savedMessage.getMessage()).isEqualTo("Test Slack Message"); // 메시지 확인
    assertThat(savedMessage.getTimestamp()).isNotNull();
    assertThat(savedMessage.getCreatedAt()).isNotNull();

    // 타임스탬프 검증: 현재 시간과 1초 이내인지 확인
    assertThat(savedMessage.getTimestamp()).isCloseTo(now, within(1, ChronoUnit.SECONDS));
    assertThat(savedMessage.getCreatedAt()).isCloseTo(now, within(1, ChronoUnit.SECONDS));
  }
}
