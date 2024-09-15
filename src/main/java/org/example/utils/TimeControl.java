package org.example.utils;

import lombok.Getter;

@Getter
public enum TimeControl {
  BLITZ_5(GameMode.BLITZ, 300),
  BLITZ_10(GameMode.BLITZ, 600),
  BULLET_3(GameMode.BULLET, 180),
  BULLET_1(GameMode.BULLET, 60);

  private final GameMode gameMode;
  private final int timeInSeconds;

  TimeControl(GameMode gameMode, int timeInSeconds) {
    this.timeInSeconds = timeInSeconds;
    this.gameMode = gameMode;
  }
}
