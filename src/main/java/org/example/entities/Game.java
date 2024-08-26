package org.example.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.utils.GameStatus;
import org.example.utils.TimeControl;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Game extends DataTransferObject {
  private TimeControl timeControl;
  private String activePlayerConnectionId;

  private List<String> moveList;

  private GameStatus gameStatus;

  private String gameStateJSON;

  private List<Player> players;

  @Override
  public String toString() {
    return null;
  }

  // add second player to game and set up
  public void setup(Player player2) {
    if (this.gameStatus.getStatus() != GameStatus.PENDING.getStatus() || players.size() != 1) {
      // game has already started or has finished so do nothing
      // TODO throw an error or something
      return;
    }
    moveList = new ArrayList<>();
    gameStateJSON = "";
    Player player1 = players.get(0);

    Random rand = new Random();

    // Generate random integers in range (0, 1) inclusive
    int randInt = rand.nextInt(2);

    player1.setRemainingTime(timeControl.getTimeInSeconds());
    player2.setRemainingTime(timeControl.getTimeInSeconds());
    if (randInt == 0) {
      player1.setIsWhite(false);
      player2.setIsWhite(true);
      activePlayerConnectionId = player2.getConnectionId();
    } else {
      player1.setIsWhite(true);
      activePlayerConnectionId = player1.getConnectionId();
      player2.setIsWhite(false);
    }
    players.add(player2);
    this.gameStatus = GameStatus.ONGOING;
  }

  // add first player to game
  public Game(TimeControl timeControl, Player player1) {
    players = new ArrayList<>();
    players.add(player1);
    this.timeControl = timeControl;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    // TODO: ask evan if this is really necessary
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, timeControl.getTimeInSeconds(), activePlayerConnectionId, gameStatus.getStatus());
  }
}
