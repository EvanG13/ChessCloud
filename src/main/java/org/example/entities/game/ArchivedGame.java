package org.example.entities.game;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.player.ArchivedPlayer;
import org.example.enums.ResultReason;

@Getter
@Setter
@SuperBuilder
public class ArchivedGame extends BaseGame<ArchivedPlayer> {

  private ResultReason resultReason;

  private String winner;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ArchivedGame { ");
    sb.append("id=").append(id);
    sb.append(", timeControl=").append(timeControl);
    sb.append(", created=").append(created);
    sb.append(", moveList=").append(moveList != null ? moveList.toString() : "[]");
    sb.append(", players=").append(players != null ? players.toString() : "[]");
    sb.append(", rating=").append(rating);
    sb.append(" }");

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArchivedGame archivedGame = (ArchivedGame) o;

    return Objects.equals(id, archivedGame.id)
        && Objects.equals(timeControl, archivedGame.timeControl)
        && Objects.equals(created, archivedGame.created)
        && Objects.equals(moveList, archivedGame.moveList)
        && Objects.equals(players, archivedGame.players)
        && Objects.equals(rating, archivedGame.rating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, timeControl, created, moveList, players, rating);
  }
}
