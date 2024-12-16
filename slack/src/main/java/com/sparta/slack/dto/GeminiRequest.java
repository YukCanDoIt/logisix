package com.sparta.slack.dto;

import java.util.List;

public record GeminiRequest(
    List<Content> contents
) {
  public GeminiRequest(String message) {
    this(List.of(new Content(List.of(new TextPart(message)))));
  }

  public static record Content(List<Part> parts) {
  }

  public interface Part {
  }

  public static record TextPart(String text) implements Part {
  }
}
