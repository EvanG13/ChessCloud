package org.example.entities;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
public class Player {
  @Expose private String username;
  @Expose private String playerId;
  @Expose private Boolean isWhite;

  @Expose private int remainingTime;

  @Expose private String connectionId;

  public String toString() {
    return "Player: username: "
        + this.username
        + " isWhite: "
        + this.isWhite
        + " remainingTime "
        + remainingTime
        + " connectionId: "
        + this.connectionId;
  }
}
