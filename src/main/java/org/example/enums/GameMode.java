package org.example.enums;

public enum GameMode {
  BULLET,
  BLITZ,
  RAPID;

  public String asKey() {
    return name().toLowerCase();
  }
}
