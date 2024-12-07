package org.example.enums;

public enum GameMode {
  BULLET,
  BLITZ,
  RAPID;

  public String asKey() {
    return name().toLowerCase();
  }

  public static GameMode fromKey(String key) {
    for (GameMode mode : GameMode.values()) if (mode.name().equalsIgnoreCase(key)) return mode;

    return null;
  }
}
