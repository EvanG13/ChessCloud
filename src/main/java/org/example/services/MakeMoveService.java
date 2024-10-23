package org.example.services;

import chariot.util.Board;
import com.mongodb.client.model.Updates;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.move.Move;
import org.example.entities.player.Player;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.exceptions.Unauthorized;

@AllArgsConstructor
public class MakeMoveService {

  private GameDbService gameDbService;

  private Board board;

  public MakeMoveService() {
    gameDbService = new GameDbService();
  }

  public MakeMoveService(GameDbService gameDbService) {
    this.gameDbService = gameDbService;
  }

  private boolean isMoveLegal(String move) {
    return board.validMoves().stream().map(Board.Move::uci).toList().contains(move);
  }

  public Game loadGame(String gameId, String connectionId, String playerId)
      throws NotFound, Unauthorized, InternalServerError {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    int index = -1;
    if (playerId.equals(player1.getPlayerId())) {
      index = 0;
    } else if (playerId.equals(player2.getPlayerId())) {
      index = 1;
    }

    if (index == -1) {
      throw new Unauthorized("User is not in this Game");
    }

    if (!players.get(index).getConnectionId().equals(connectionId)) {
      throw new InternalServerError("Connection IDs dont match");
    }

    board = Board.fromFEN(game.getGameStateAsFen());
    return game;
  }

  public String[] getPlayerConnectionIds(Game game) {
    String[] connectionIds = new String[2];

    connectionIds[0] = game.getPlayers().get(0).getConnectionId();
    connectionIds[1] = game.getPlayers().get(1).getConnectionId();

    return connectionIds;
  }

  public boolean isPlayersTurn(Game game, String playerId) {
    List<Player> players = game.getPlayers();
    Player player = players.get(0).getPlayerId().equals(playerId) ? players.get(0) : players.get(1);

    return game.getIsWhitesTurn() == player.getIsWhite();
  }

  public Game makeMove(String moveUCI, Game game, Date time) throws BadRequest {
    if (!isMoveLegal(moveUCI)) {
      throw new BadRequest("Illegal Move: " + moveUCI);
    }

    String san = board.toSAN(moveUCI);
    board = board.play(moveUCI);
    Date lastModified = game.getLastModified();

    long t = ((time.getTime() - lastModified.getTime())) / 1000; // convert to seconds from millis

    Move move =
        Move.builder().moveAsUCI(moveUCI).moveAsSan(san).duration((int) Math.max(t, 1)).build();

    List<Player> updatedPlayers = game.getPlayers();
    Player activePlayer;
    boolean isWhiteTurn = game.getIsWhitesTurn();
    if ((updatedPlayers.getFirst().getIsWhite() && isWhiteTurn)
        || (!updatedPlayers.getFirst().getIsWhite() && !isWhiteTurn)) {
      activePlayer = game.getPlayers().getFirst();
    } else {
      activePlayer = game.getPlayers().getLast();
    }
    activePlayer.setRemainingTime(activePlayer.getRemainingTime() - move.getDuration());
    String updatedGameFen = board.toStandardFEN();
    gameDbService.patch(
        game.getId(),
        Updates.combine(
            Updates.set("gameStateAsFen", updatedGameFen),
            Updates.set("isWhitesTurn", !game.getIsWhitesTurn()),
            Updates.set("lastModified", time),
            Updates.set("players", updatedPlayers),
            Updates.push("moveList", move)));

    game.getMoveList().add(move);
    game.setGameStateAsFen(updatedGameFen);
    game.setIsWhitesTurn(!game.getIsWhitesTurn());

    return game;
  }

  public Map<String, Integer> getRemainingTimes(Game game) {
    List<Player> players = game.getPlayers();
    Player whitePlayer;
    Player blackPlayer;
    if (players.getFirst().getIsWhite()) {
      whitePlayer = players.getFirst();
      blackPlayer = players.getLast();
    } else {
      whitePlayer = players.getLast();
      blackPlayer = players.getFirst();
    }
    int whiteTime = whitePlayer.getRemainingTime();
    int blackTime = blackPlayer.getRemainingTime();
    return Map.of("white", whiteTime, "black", blackTime);
  }
}
