package org.example.entities.archivedGame;

import com.google.gson.annotations.Expose;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.example.entities.DataTransferObject;
import org.example.entities.Player;
import org.example.entities.game.Game;
import org.example.enums.TimeControl;

@Getter
@Setter
public class ArchivedGame extends DataTransferObject {

  @Expose private TimeControl timeControl;

  @Expose private Date created;

  @Expose private List<String> moveList;

  @Expose private List<Player> players;

  @Expose private Integer rating;

  public void gameToArchivedGame(@NonNull Game game) {
    created = new Date();

    this.players.addAll(game.getPlayers());
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
