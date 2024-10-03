package org.example.services;

import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.Player;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.enums.Action;
import org.example.enums.GameMode;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.models.responses.websocket.GameOverMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.utils.MongoDBUtility;
import org.example.utils.socketMessenger.SocketMessenger;

@AllArgsConstructor
@Setter
@Getter
public class GameOverService {

  // TODO : Investigate how to make this quicker

  private ResultReason resultReason;
  private String losingPlayerId;
  private String winningPlayerId;
  private String losingPlayerUsername;
  private String winningPlayerUsername;
  private Game game;
  private SocketMessenger socketMessenger;
  private GameDbService gameDbService;
  private StatsDbService statsDbService;
  private Stats losingPlayerStats;
  private Stats winningPlayerStats;

  /**
   * finds a game based on the losingPlayerId if the game is not found then throws NotFound
   * exception if the game is a draw, then the event body coming from client should set whoever's
   * turn it is as the losingPlayerId and have them be the client that sends the socket message
   */
  public GameOverService(
      ResultReason resultReason, String losingPlayerId, SocketMessenger messenger)
      throws NotFound, InternalServerError {
    this.gameDbService = new GameDbService();
    this.game = gameDbService.getGameFromUserID(losingPlayerId);

    if (this.game.getGameStatus().equals(GameStatus.PENDING)) {
      gameDbService.deleteGame(game.getId());
      return;
    }

    this.resultReason = resultReason;
    this.losingPlayerId = losingPlayerId;
    this.statsDbService = new StatsDbService();
    this.socketMessenger = messenger;

    Player player1 = game.getPlayers().get(0);
    Player player2 = game.getPlayers().get(1);
    if (player1.getPlayerId().equals(losingPlayerId)) {
      this.winningPlayerId = player2.getPlayerId();
      this.winningPlayerUsername = player2.getUsername();
      this.losingPlayerUsername = player1.getUsername();
    } else {
      this.winningPlayerId = player1.getPlayerId();
      this.winningPlayerUsername = player1.getUsername();
      this.losingPlayerUsername = player2.getUsername();
    }

    this.winningPlayerStats = statsDbService.getStatsByUserID(winningPlayerId);
    this.losingPlayerStats = statsDbService.getStatsByUserID(losingPlayerId);

    emitOutcome();
    updateGame();
    updateRatings();
  }

  private void emitOutcome() throws InternalServerError {
    String messageJson =
        new SocketResponseBody<GameOverMessageData>(
                Action.GAME_OVER,
                new GameOverMessageData(resultReason, winningPlayerUsername, losingPlayerUsername))
            .toJSON();
    socketMessenger.sendMessages(losingPlayerId, winningPlayerId, messageJson);
  }

  public void archiveGame() {
    // TODO: implement me!
    System.out.println("Implement the archiveGame function!");
  }

  // TODO : Send this game to a finished game collection
  public void updateGame() {
    MongoDBUtility<Game> gameUtility = new MongoDBUtility<>("games", Game.class);
    gameUtility.patch(game.getId(), Updates.set("gameStatus", GameStatus.FINISHED.toString()));
  }

  public void updateRatings() throws InternalServerError {
    GameMode gameMode = game.getTimeControl().getGameMode();

    Stats.GameModeStats winningGameModeStats = winningPlayerStats.getGamemodeStats(gameMode);
    Stats.GameModeStats losingGameModeStats = losingPlayerStats.getGamemodeStats(gameMode);

    switch (resultReason) {
      // Someone abandoned the game: AFK, abandoned / logged out early on
      case ABORTED -> {
        return;
      }

      // Someone won the game
      case FORFEIT, TIMEOUT, CHECKMATE -> {
        winningGameModeStats.AddWin(losingGameModeStats.getRating(), losingGameModeStats.getRD());
        losingGameModeStats.AddLoss(winningGameModeStats.getRating(), winningGameModeStats.getRD());
      }
      // Game was a draw
      case REPETITION, INSUFFICIENT_MATERIAL -> {
        winningGameModeStats.AddDraw(losingGameModeStats.getRating(), losingGameModeStats.getRD());
        losingGameModeStats.AddDraw(winningGameModeStats.getRating(), winningGameModeStats.getRD());
      }
      default -> throw new InternalServerError("Unsupported ResultReason: " + resultReason);
    }

    MongoDBUtility<Stats> statsUtility = new MongoDBUtility<>("stats", Stats.class);
    statsUtility.patch(
        winningPlayerId, Updates.set("gameModeStats." + gameMode.asKey(), winningGameModeStats));
    statsUtility.patch(
        losingPlayerId, Updates.set("gameModeStats." + gameMode.asKey(), losingGameModeStats));
  }
}
