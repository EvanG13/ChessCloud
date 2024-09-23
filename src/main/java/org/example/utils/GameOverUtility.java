package org.example.utils;

import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.Game;
import org.example.entities.Stats;
import org.example.enums.GameMode;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
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
    // can throw an InternalServerError
    this.winningPlayerStats = statsService.getStatsByUserID(winningPlayerId);
    this.losingPlayerStats = statsService.getStatsByUserID(losingPlayerId);
  }

  public void emitOutcome() throws InternalServerError {
    String messageJson = new GameOverResponseBody(resultReason, winningPlayerUsername, losingPlayerUsername).toJSON();
    socketMessenger.sendMessages(losingPlayerId, winningPlayerId, messageJson);
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

  public void updateRatings() throws InternalServerError {
    GameMode gameMode = game.getTimeControl().getGameMode();

    Stats.GameModeStats winningGameModeStats = winningPlayerStats.getGamemodeStats(gameMode);
    Stats.GameModeStats losingGameModeStats = losingPlayerStats.getGamemodeStats(gameMode);

    switch (resultReason) {
      // Someone abandoned the game: AFK, abandoned / logged out early on
      case ABORTED: //return;  maybe?
      // Someone won the game
      case FORFEIT:
      case TIMEOUT:
      case CHECKMATE:
        winningGameModeStats.AddWin(losingGameModeStats.getRating(), losingGameModeStats.getRD());
        losingGameModeStats.AddLoss(winningGameModeStats.getRating(), winningGameModeStats.getRD());
        break;
      // Game was a draw
      case REPETITION:
      case INSUFFICIENT_MATERIAL:
        winningGameModeStats.AddDraw(losingGameModeStats.getRating(), losingGameModeStats.getRD());
        losingGameModeStats.AddDraw(winningGameModeStats.getRating(), winningGameModeStats.getRD());
        break;
      // Error if result reason not accounted for
      default:
        throw new InternalServerError("Unsupported ResultReason: " + resultReason);
    }

    MongoDBUtility<Stats> statsUtility = new MongoDBUtility<>("stats", Stats.class);
    statsUtility.patch(winningPlayerId, Updates.set("gameModeStats." + gameMode.asKey(), winningGameModeStats));
    statsUtility.patch(losingPlayerId, Updates.set("gameModeStats." + gameMode.asKey(), losingGameModeStats));
  }
}
