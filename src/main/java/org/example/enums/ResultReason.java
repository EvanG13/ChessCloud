package org.example.enums;

import lombok.Getter;

@Getter
public enum ResultReason {
  ABORTED("game aborted"),

  FORFEIT("win by opponent forfeit"),
  TIMEOUT("win by opponent running out of time"),
  CHECKMATE("win by checkmate"),

  MUTUAL_DRAW("draw by mutual agreement"),
  REPETITION("draw by repetition"),
  INSUFFICIENT_MATERIAL("draw by insufficient material");

  private final String message;

  ResultReason(String message) {
    this.message = message;
  }
}
