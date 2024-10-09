package org.example.entities.game;

import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.player.ArchivedPlayer;
import org.example.enums.ResultReason;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ArchivedGame extends BaseGame<ArchivedPlayer> {

  private ResultReason resultReason;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ArchivedGame { ")
        .append("id=")
        .append(id)
        .append(", timeControl=")
        .append(timeControl)
        .append(", created=")
        .append(created)
        .append(", moveList=")
        .append(moveList != null ? moveList.toString() : "[]")
        .append(", players=")
        .append(players != null ? players.toString() : "[]")
        .append(", rating=")
        .append(rating)
        .append(", resultReason=")
        .append(resultReason)
        .append(" }");

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ArchivedGame that = (ArchivedGame) o;
    return resultReason == that.resultReason;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), resultReason);
  }
}
