package org.example.entities.game;

import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.example.constants.ChessConstants;
import org.example.entities.player.Player;
import org.example.entities.timeControl.TimeControl;
import org.example.enums.GameMode;
import org.example.enums.GameStatus;
import org.example.exceptions.NotFound;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Game extends BaseGame<Player> {

  private Boolean isWhitesTurn;

  private Date lastModified;

  private GameStatus gameStatus;

  private String gameStateAsFen;

  public Game(TimeControl timeControl, Player player) throws NotFound {
    this.id = new ObjectId().toString();

    this.timeControl = timeControl;
    this.isWhitesTurn = true;
    this.gameStatus = GameStatus.PENDING;
    this.players = Arrays.asList(new Player[2]);
    this.players.set(0, player);
    this.rating = player.getRating();
    this.gameStateAsFen = ChessConstants.STARTING_FEN_STRING;
    this.moveList = new ArrayList<>();
    this.gameMode = GameMode.fromTime(timeControl.getBase());
  }

  /**
   * Add player to existing game
   *
   * @param player2 the second player to be added to the game
   */
  public void setup(Player player2) throws Exception {
    if (this.gameStatus.getStatus() != GameStatus.PENDING.getStatus()) {
      throw new Exception("Game has already started or has finished");
    }

    Player player1 = players.getFirst();

    player1.setRemainingTime(timeControl.getBase());
    player2.setRemainingTime(timeControl.getBase());

    Random rand = new Random();
    int randInt = rand.nextInt(2);
    if (randInt == 0) {
      player1.setIsWhite(false);
      player2.setIsWhite(true);
    } else {
      player1.setIsWhite(true);
      player2.setIsWhite(false);
    }

    this.gameStatus = GameStatus.ONGOING;
    this.players.set(1, player2);
    this.lastModified = new Date();
  }

  public boolean containsConnectionId(String connectionId) {
    return this.players.stream().anyMatch(player -> player.getConnectionId().equals(connectionId));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Game{");
    sb.append("id='").append(id).append('\'');
    sb.append(", rating=").append(rating);
    sb.append(", players=").append(players);
    sb.append(", moveList=").append(moveList);
    sb.append(", created=").append(created);
    sb.append(", timeControl=").append(timeControl);
    sb.append(", gameStateAsFen='").append(gameStateAsFen).append('\'');
    sb.append(", gameStatus=").append(gameStatus);
    sb.append(", lastModified=").append(lastModified);
    sb.append(", isWhitesTurn=").append(isWhitesTurn);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Game game = (Game) o;
    return Objects.equals(isWhitesTurn, game.isWhitesTurn)
        && Objects.equals(lastModified, game.lastModified)
        && gameStatus == game.gameStatus
        && Objects.equals(gameStateAsFen, game.gameStateAsFen);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isWhitesTurn, lastModified, gameStatus, gameStateAsFen);
  }
}
