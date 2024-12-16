package com.sparta.slack.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "slack_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlackMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "channel", nullable = false)
  private String channel;

  @Column(name = "message", nullable = false, length = 2000) // 길이 확장
  private String message;

  @Column(name = "timestamp")
  private LocalDateTime timestamp;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
