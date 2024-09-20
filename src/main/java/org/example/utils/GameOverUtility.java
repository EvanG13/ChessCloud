package org.example.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.Game;
import org.example.entities.Stats;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.models.responses.GameOverResponseBody;
import org.example.services.GameStateService;
import org.example.services.StatsService;
import org.example.utils.socketMessenger.SocketMessenger;

@AllArgsConstructor
@SuperBuilder
@Setter
@Getter
public class GameOverUtility {

  private boolean isDraw;
  private ResultReason resultReason;
  private String losingPlayerId;
  private String winningPlayerId;
  private String losingPlayerUsername;
  private String winningPlayerUsername;
  private Game game;
  private SocketMessenger socketMessenger;
  private GameStateService gameService;
  private StatsService statsService;
  private Stats losingPlayerStats;
  private Stats winningPlayerStats;

  /*
   * finds a game based on the losingPlayerId
   * if the game is not found then throws NotFound exception
   * if the game is a draw, then the eventbody coming from client should set whoever's turn it is as the losingPlayerId
   * and have them be the client that sends the socket message
   * */
  public GameOverUtility(boolean isDraw, ResultReason resultReason, String losingPlayerId)
      throws NotFound, InternalServerError {
    this.isDraw = isDraw;
    this.resultReason = resultReason;
    this.losingPlayerId = losingPlayerId;
    this.statsService = new StatsService();
    this.gameService = new GameStateService();
    // get the game object via the losingPlayerId
    this.game = gameService.getGame(losingPlayerId); // can throw NotFound
    if (game.getPlayers().get(0).getPlayerId().equals(losingPlayerId)) {
      this.winningPlayerId = game.getPlayers().get(1).getPlayerId();
      this.winningPlayerUsername = game.getPlayers().get(1).getUsername();
      this.losingPlayerUsername = game.getPlayers().get(0).getUsername();
    } else {
      this.winningPlayerId = game.getPlayers().get(0).getPlayerId();
      this.winningPlayerUsername = game.getPlayers().get(0).getUsername();
      this.losingPlayerUsername = game.getPlayers().get(1).getUsername();
    }

    // get the stats of both players (can throw InternalServerError)
    this.winningPlayerStats = statsService.getStatsByUserID(winningPlayerId);
    this.losingPlayerStats = statsService.getStatsByUserID(losingPlayerId);
  }

  public void emitOutcome() {
    try {
      String messageJson =
          new GameOverResponseBody(resultReason, winningPlayerUsername, losingPlayerUsername)
              .toJSON();
      socketMessenger.sendMessages(losingPlayerId, winningPlayerId, messageJson);
    } catch (BadRequest exception) {
      System.out.println("exception thrown in emitOutcome");
      System.out.println(exception.getMessage());
    }
  }

  public void archiveGame() {
    // TODO: implement me!
    System.out.println("Implement the archiveGame function!");
  }

  public void updateGame() {
    game.setGameStatus(GameStatus.FINISHED);
    MongoDBUtility<Game> gameUtility = new MongoDBUtility<>("games", Game.class);
    gameUtility.put(game.getId(), game);
  }

  public void updateRatings() {
    // TODO: implement me!
    System.out.println("Implement the updateRatings function -- ask caimin about this!");
  }
}
