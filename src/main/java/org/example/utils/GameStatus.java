package org.example.utils;

public enum GameStatus {
  PENDING(1),
  ONGOING(2),

  FINISHED(3);

  private final int statusString;

  GameStatus(int status) {
    statusString = status;
  }

  public int getStatus() {
    return statusString;
  }
}
