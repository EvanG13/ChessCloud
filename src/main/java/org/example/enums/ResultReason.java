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
  INSUFFICIENT_MATERIAL("draw by insufficient material"),
  STALEMATE("draw by stalemate"),
  FIFTY_MOVE_RULE("draw by 50 move rule");
  private final String message;

  ResultReason(String message) {
    this.message = message;
  }
}
