package org.example.entities.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.example.constants.ChessConstants;
import org.example.entities.player.Player;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;

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

  public Game(TimeControl timeControl, Player player) {
    this.id = new ObjectId().toString();
    this.lastModified = new Date();

    this.timeControl = timeControl;
    // TODO: not omit moveList?
    this.isWhitesTurn = true;
    this.gameStatus = GameStatus.PENDING;
    this.players =
        new ArrayList<>() {
          {
            add(player);
          }
        };
    this.rating = player.getRating();
    // TODO: not omit gameStateAsFen?
  }

  /**
   * Add player to existing game
   *
   * @param player2 the second player to be added to the game
   */
  public void setup(Player player2) throws Exception {
    if (this.gameStatus.getStatus() != GameStatus.PENDING.getStatus() || players.size() != 1) {
      throw new Exception("Game has already started or has finished");
    }

    Player player1 = players.getFirst();

    player1.setRemainingTime(timeControl.getTimeInSeconds());
    player2.setRemainingTime(timeControl.getTimeInSeconds());

    Random rand = new Random();
    int randInt = rand.nextInt(2);
    if (randInt == 0) {
      player1.setIsWhite(false);
      player2.setIsWhite(true);
    } else {
      player1.setIsWhite(true);
      player2.setIsWhite(false);
    }

    this.moveList = new ArrayList<>();
    this.gameStateAsFen = ChessConstants.STARTING_FEN_STRING;
    this.gameStatus = GameStatus.ONGOING;
    this.players.add(player2);
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
