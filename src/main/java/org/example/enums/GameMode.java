package org.example.enums;

import lombok.Getter;
import org.example.exceptions.NotFound;

@Getter
public enum GameMode {
  BULLET(15, 180),
  BLITZ(180, 600),
  RAPID(600, 1800);

  private final int timeMin; // inclusive
  private final int timeMax; // exclusive

  GameMode(int timeMin, int timeMax) {
    this.timeMin = timeMin;
    this.timeMax = timeMax;
  }

  public String asKey() {
    return name().toLowerCase();
  }

  public static GameMode fromKey(String key) {
    for (GameMode mode : GameMode.values()) if (mode.name().equalsIgnoreCase(key)) return mode;

    return null;
  }

  public static GameMode fromTime(int time) throws NotFound {
    for (GameMode mode : GameMode.values())
      if (mode.timeMin <= time && time < mode.timeMax) return mode;

    throw new NotFound("Game mode not found");
  }
}
