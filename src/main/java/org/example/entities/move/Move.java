package org.example.entities.move;

import com.google.gson.annotations.Expose;

public class Move {
  @Expose String moveAsToFrom;
  @Expose String moveAsSan;
  @Expose Integer duration;

  public Move(String moveAsToFrom, String gameFen, int duration) {}
}
