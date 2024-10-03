package org.example.services;

import chariot.util.Board;
import com.mongodb.client.model.Updates;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.entities.Player;
import org.example.entities.game.Game;
import org.example.entities.game.GameService;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.exceptions.Unauthorized;

@AllArgsConstructor
public class MakeMoveService {

  private GameService gameService;

  private Board board;

  public MakeMoveService() {
    gameService = new GameService();
  }

  public MakeMoveService(GameService gameService) {
    this.gameService = gameService;
  }

  private boolean isMoveLegal(String move) {
    return board.validMoves().stream().map(Board.Move::uci).toList().contains(move);
  }

  public Game loadGame(String gameId, String connectionId, String playerId)
      throws NotFound, Unauthorized, InternalServerError {
    Game game = gameService.get(gameId);

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

  public Game makeMove(String moveUCI, Game game) throws BadRequest {
    if (!isMoveLegal(moveUCI)) {
      throw new BadRequest("Illegal Move: " + moveUCI);
    }

    board = board.play(moveUCI);

    String updatedGameFen = board.toStandardFEN();
    gameService.patch(
        game.getId(),
        Updates.combine(
            Updates.set("gameStateAsFen", updatedGameFen),
            Updates.set("isWhitesTurn", !game.getIsWhitesTurn()),
            Updates.push("moveList", moveUCI)));

    game.getMoveList().add(moveUCI);
    game.setGameStateAsFen(updatedGameFen);
    game.setIsWhitesTurn(!game.getIsWhitesTurn());

    return game;
  }
}
