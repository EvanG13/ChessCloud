package org.example.entities.game;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.DataTransferObject;
import org.example.entities.move.Move;
import org.example.entities.player.BasePlayer;
import org.example.enums.TimeControl;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseGame<T extends BasePlayer> extends DataTransferObject {
  protected TimeControl timeControl;
  protected Date created;
  protected List<Move> moveList;
  protected List<T> players;
  protected Integer rating;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseGame<?> baseGame = (BaseGame<?>) o;

    return timeControl == baseGame.timeControl
        && Objects.equals(created, baseGame.created)
        && Objects.equals(moveList, baseGame.moveList)
        && Objects.equals(players, baseGame.players)
        && Objects.equals(rating, baseGame.rating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeControl, created, moveList, players, rating);
  }
}
