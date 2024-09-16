package org.example.handlers.makeMove;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.mongodb.client.model.Updates;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.databases.MongoDBUtility;
import org.example.entities.Game;
import org.example.entities.Player;

@AllArgsConstructor
public class MakeMoveService {

  private MongoDBUtility<Game> gameDBUtility;

  private Board board;

  public MakeMoveService() {
    gameDBUtility = new MongoDBUtility<>("games", Game.class);
    board = new Board();
  }

  public String getBoardState(String gameId) {
    Optional<Game> g = gameDBUtility.get(gameId);

    if (g.isEmpty()) return null;

    Game game = g.get();

    return game.getGameStateAsFen();
  }

  public boolean doesGameExist(String gameId) {
    Optional<Game> optionalGame = gameDBUtility.get(gameId);

    return optionalGame.isPresent();
  }

  public boolean isMoveLegal(String boardState, Move move) {
    board.loadFromFen(boardState);
    return board.legalMoves().contains(move);
  }

  public boolean doesGameMatchUser(String gameId, String connectionId, String playerId) {
    Optional<Game> optionalGame = gameDBUtility.get(gameId);

    if (optionalGame.isEmpty()) return false;

    Game game = optionalGame.get();

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    if (playerId.equals(player1.getPlayerId())) {
      return player1.getConnectionId().equals(connectionId);
    } else if (playerId.equals(player2.getPlayerId())) {
      return player2.getConnectionId().equals(connectionId);
    }

    return false;
  }

  public boolean isMovingOutOfTurn(String gameId, String connectionId) {
    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    if (optionalGame.isEmpty()) {
      System.out.println(" game not found.");
      return false;
    }

    Game game = optionalGame.get();
    return game.getActivePlayerConnectionId() != connectionId;
  }

  public String makeMove(String moveString, String boardState, String gameId) {
    board.loadFromFen(boardState);
    Move move;
    try {
      move = new Move(moveString, board.getSideToMove());
    } catch (Exception e) {
      return "INVALID MOVE";
    }
    if (!isMoveLegal(boardState, move)) {
      return "INVALID MOVE";
    }
    try {
      board.doMove(move);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return "INVALID MOVE";
    }
    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    Game game = optionalGame.get();

    String nextConnectionId =
        game.getPlayers().get(0).getConnectionId() == game.getActivePlayerConnectionId()
            ? game.getPlayers().get(1).getConnectionId()
            : game.getPlayers().get(0).getConnectionId();
    System.out.println(
        "current :" + game.getActivePlayerConnectionId() + " next " + nextConnectionId);
    gameDBUtility.patch(
        gameId,
        Updates.combine(
            Updates.set("gameStateAsFen", board.getFen()),
            Updates.set("activePlayerConnectionId", nextConnectionId)));

    return board.getFen();
  }
}
