package org.example.entities;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.example.utils.GameStatus;
import org.example.utils.TimeControl;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Game extends DataTransferObject {
  @Expose private TimeControl timeControl;
  private String activePlayerConnectionId;

  private List<String> moveList;

  @Expose private GameStatus gameStatus;

  private String gameStateJSON;

  @Expose private List<Player> players;

  @Expose private Integer rating;

  @Override
  public String toString() {
    return "Game{"
        + "id= "
        + id
        + "\n"
        + ", timeControl= "
        + timeControl
        + "\n"
        + ", activePlayerConnectionId= "
        + activePlayerConnectionId
        + '\n'
        + ", moveList="
        + moveList
        + "\n"
        + ", gameStatus= "
        + gameStatus
        + "\n"
        + ", gameStateJSON= "
        + gameStateJSON
        + "\n"
        + ", players= "
        + players
        + "\n, rating= "
        + rating
        + "\n"
        + '}';
  }

  /**
   * Add player to existing game
   *
   * @param player2
   */
  public void setup(Player player2) throws Exception {
    if (this.gameStatus.getStatus() != GameStatus.PENDING.getStatus() || players.size() != 1) {
      // game has already started or has finished so do nothing
      throw new Exception();
    }

    moveList = new ArrayList<>();
    gameStateJSON = "";
    Player player1 = players.get(0);

    Random rand = new Random();

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

  public Game(TimeControl timeControl, Player player) {
    id = new ObjectId().toString();
    players = new ArrayList<>();
    players.add(player);
    this.timeControl = timeControl;
    this.gameStatus = GameStatus.PENDING;
    this.rating = player.getRating();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Game game = (Game) o;
    return Objects.equals(id, game.id)
        && timeControl == game.timeControl
        && Objects.equals(activePlayerConnectionId, game.activePlayerConnectionId)
        && gameStatus == game.gameStatus
        && Objects.equals(players, game.players)
        && Objects.equals(rating, game.rating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, timeControl, activePlayerConnectionId, gameStatus, players, rating);
  }
}
