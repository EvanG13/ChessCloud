package org.example.entities.player;

import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArchivedPlayer extends BasePlayer {
  private Boolean isWinner;

  @Override
  public String toString() {
    return "ArchivedPlayer { "
        + "playerId="
        + playerId
        + ", username='"
        + username
        + '\''
        + ", rating="
        + rating
        + ", isWhite="
        + isWhite
        + ", isWinner="
        + isWinner
        + " }";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ArchivedPlayer that = (ArchivedPlayer) o;
    return Objects.equals(isWinner, that.isWinner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isWinner);
  }
}
