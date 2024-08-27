package org.example.utils;

public enum TimeControl {
  BLITZ_5(300),
  BLITZ_10(600),

  BULLET_3(180),

  BULLET_1(60);

  private final int timeInSeconds;

  TimeControl(int timeInSeconds) {
    this.timeInSeconds = timeInSeconds;
  }

  public int getTimeInSeconds() {
    return timeInSeconds;
  }
}
