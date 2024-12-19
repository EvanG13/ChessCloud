package org.example.handlers.websocket.gameOver;

import chariot.util.Board;
import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.entities.game.*;
import org.example.entities.player.Player;
import org.example.entities.stats.GameModeStats;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.enums.GameMode;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
import org.example.enums.WebsocketResponseAction;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.models.responses.websocket.GameOverMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
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
  private GameUtility gameUtility;
  private StatsUtility statsUtility;
  private Stats losingPlayerStats;
  private Stats winningPlayerStats;
  private ArchivedGameUtility archivedGameUtility;

  /**
   * finds a game based on the losingPlayerId if the game is not found then throws NotFound
   * exception if the game is a draw, then the event body coming from client should set whoever's
   * turn it is as the losingPlayerId and have them be the client that sends the socket message
   */
  public GameOverService(
      ResultReason resultReason, String losingPlayerId, SocketMessenger messenger)
      throws NotFound, InternalServerError {
    this.gameUtility = new GameUtility();
    this.game = gameUtility.getGameFromUserId(losingPlayerId);

    if (this.game.getGameStatus().equals(GameStatus.PENDING)) {
      gameUtility.delete(game.getId());
      return;
    }
    this.archivedGameUtility = new ArchivedGameUtility();
    this.resultReason = resultReason;
    this.losingPlayerId = losingPlayerId;
    this.statsUtility = new StatsUtility();
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

    this.winningPlayerStats = statsUtility.getStatsByUserID(winningPlayerId);
    this.losingPlayerStats = statsUtility.getStatsByUserID(losingPlayerId);
  }

  public GameOverService(
      ResultReason resultReason, Game game, String losingPlayerId, SocketMessenger messenger)
      throws InternalServerError {
    this.gameUtility = new GameUtility();
    this.game = game;

    if (this.game.getGameStatus().equals(GameStatus.PENDING)) {
      gameUtility.delete(game.getId());
      return;
    }

    this.archivedGameUtility = new ArchivedGameUtility();
    this.resultReason = resultReason;
    this.losingPlayerId = losingPlayerId;
    this.statsUtility = new StatsUtility();
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

    this.winningPlayerStats = statsUtility.getStatsByUserID(winningPlayerId);
    this.losingPlayerStats = statsUtility.getStatsByUserID(losingPlayerId);
  }

  public void endGame() throws InternalServerError {

    emitOutcome();

    updateGame();

    updateRatings();

    archiveGame();
  }

  private boolean isGameOver(String FEN) {
    Board board = Board.fromFEN(game.getGameStateAsFen());
    return board.ended();
  }

  private void emitOutcome() throws InternalServerError {
    String connId = this.game.getPlayers().get(0).getConnectionId();
    String connId2 = this.game.getPlayers().get(1).getConnectionId();

    String messageJson =
        new SocketResponseBody<>(
                WebsocketResponseAction.GAME_OVER,
                new GameOverMessageData(resultReason, winningPlayerUsername, losingPlayerUsername))
            .toJSON();

    socketMessenger.sendMessages(connId, connId2, messageJson);
  }

  public void archiveGame() {
    gameUtility.delete(this.game.getId());
    archivedGameUtility.archiveGame(this.game, winningPlayerUsername, resultReason);
  }

  public void updateGame() {
    gameUtility.patch(game.getId(), Updates.set("gameStatus", GameStatus.FINISHED.toString()));
  }

  public void updateRatings() throws InternalServerError {
    GameMode gameMode = game.getGameMode();

    GameModeStats winningGameModeStats = winningPlayerStats.getGamemodeStats(gameMode);
    GameModeStats losingGameModeStats = losingPlayerStats.getGamemodeStats(gameMode);

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
      case MUTUAL_DRAW, REPETITION, INSUFFICIENT_MATERIAL, STALEMATE, FIFTY_MOVE_RULE -> {
        winningGameModeStats.AddDraw(losingGameModeStats.getRating(), losingGameModeStats.getRD());
        losingGameModeStats.AddDraw(winningGameModeStats.getRating(), winningGameModeStats.getRD());
      }
      default -> throw new InternalServerError("Unsupported ResultReason: " + resultReason);
    }

    statsUtility.put(winningPlayerId, winningPlayerStats);
    statsUtility.put(losingPlayerId, losingPlayerStats);
  }
}
