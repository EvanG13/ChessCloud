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
    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    if (optionalGame.isEmpty()) return null; // TODO: throw an exception instead

    Game game = optionalGame.get();
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

  public boolean isUserInGame(String gameId, String connectionId, String playerId) {
    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    if (optionalGame.isEmpty()) return false; // TODO: throw an exception instead

    Game game = optionalGame.get();

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    if (playerId.equals(player1.getPlayerId())) {
      return player1.getConnectionId().equals(connectionId);
    }
    if (playerId.equals(player2.getPlayerId())) {
      return player2.getConnectionId().equals(connectionId);
    }

    return false;
  }

  public String[] getPlayerConnectionIds(String gameId) {
    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    Game game = optionalGame.get(); // TODO: throw an exception if invalid

    String[] connectionIds = new String[2];
    connectionIds[0] = game.getPlayers().get(0).getConnectionId();
    connectionIds[1] = game.getPlayers().get(1).getConnectionId();
    return connectionIds;
  }

  public boolean isMovingOutOfTurn(String gameId, String connectionId) {
    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    if (optionalGame.isEmpty()) {
      System.out.println(" game not found.");
      return false; // TODO: throw an exception instead
    }

    Game game = optionalGame.get();
    return !game.getActivePlayerConnectionId().equals(connectionId);
  }

  public String makeMove(String moveString, String boardState, String gameId) {
    board.loadFromFen(boardState);

    Move move;
    // TODO: just let the exception get thrown and catch in handler, or throw our own exception
    try {
      // Try to build a move object
      move = new Move(moveString, board.getSideToMove());
    } catch (Exception e) {
      // Throws an error if the move string was not in Square-To-From format (ex: "e2e4" to try to move piece at E2 to square E4)
      // Blame the chess lib
      return "INVALID MOVE";
    }

    // Check move is legal (is among the available moves)
    if (!isMoveLegal(boardState, move)) {
      // TODO: throw our own exception probably
      return "INVALID MOVE";
    }

    board.doMove(move);

    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    Game game = optionalGame.get(); // TODO: throw an exception if invalid

    String nextConnectionId =
        game.getPlayers().get(0).getConnectionId().equals(game.getActivePlayerConnectionId())
            ? game.getPlayers().get(1).getConnectionId()
            : game.getPlayers().get(0).getConnectionId();
    System.out.println(
        "current :" + game.getActivePlayerConnectionId() + " next " + nextConnectionId);

    gameDBUtility.patch(
        gameId,
        Updates.combine(
            Updates.set("gameStateAsFen", board.getFen()),
            Updates.set("activePlayerConnectionId", nextConnectionId)
        )
    );
    return board.getFen();
  }
}
