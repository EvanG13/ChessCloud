package org.example.entities;

import com.google.gson.annotations.Expose;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Player {
  @Expose private String username;
  @Expose private String playerId;
  @Expose private Boolean isWhite;

  @Expose private Integer remainingTime;

  @Expose private String connectionId;

  @Override
  public String toString() {
    return "Player{"
        + "username='"
        + username
        + '\''
        + ", isWhite="
        + isWhite
        + ", remainingTime="
        + remainingTime
        + ", connectionId='"
        + connectionId
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Player player = (Player) o;
    return Objects.equals(isWhite, player.isWhite)
        && Objects.equals(remainingTime, player.remainingTime)
        && Objects.equals(username, player.username)
        && Objects.equals(connectionId, player.connectionId);
  }
}
