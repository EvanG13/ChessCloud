package org.example.entities.player;

import java.util.Objects;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.annotations.GsonExcludeField;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Player extends BasePlayer {

  private Integer remainingTime;
  @GsonExcludeField private String connectionId;
  @GsonExcludeField @Builder.Default private Boolean wantsDraw = false;

  @Override
  public String toString() {
    return "Player{"
        + "playerId='"
        + playerId
        + "', "
        + "username='"
        + username
        + "', "
        + "connectionId='"
        + connectionId
        + "', "
        + "rating="
        + username
        + ", "
        + "isWhite="
        + isWhite
        + ", "
        + "remainingTime="
        + remainingTime
        + ", "
        + "wantsDraw="
        + wantsDraw
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    Player player = (Player) o;
    return Objects.equals(remainingTime, player.remainingTime)
        && Objects.equals(connectionId, player.connectionId)
        && Objects.equals(wantsDraw, player.wantsDraw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), remainingTime, connectionId, wantsDraw);
  }
}
