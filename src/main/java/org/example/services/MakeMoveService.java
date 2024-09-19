package org.example.services;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.mongodb.client.model.Updates;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class MakeMoveService {

  private MongoDBUtility<Game> gameDBUtility;

  private Board board;

  public MakeMoveService() {
    gameDBUtility = new MongoDBUtility<>("games", Game.class);
    board = new Board();
  }

  public Game loadGame(String gameId) throws InternalServerError {
    return gameDBUtility.get(gameId).orElseThrow(() -> new InternalServerError("Game not found"));
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

  public String makeMove(String moveString, String boardState, String gameId) throws BadRequest {
    board.loadFromFen(boardState);

    Move move;
    try {
      move = new Move(moveString, board.getSideToMove());
    } catch (Exception e) {
      throw new BadRequest(moveString + " is invalid syntax");
    }

    // Check move is legal (is among the available moves)
    if (!isMoveLegal(boardState, move)) {
      throw new BadRequest("Illegal Move: " + moveString);
    }

    board.doMove(move);

    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    Game game = optionalGame.get(); // TODO: throw an exception if invalid

    gameDBUtility.patch(
        gameId,
        Updates.combine(
            Updates.set("gameStateAsFen", board.getFen()),
            Updates.set("isWhitesTurn", !game.getIsWhitesTurn()),
            Updates.push("moveList", move.toString())));
    return board.getFen();
  }

  public List<String> getMoveList(String gameId) {
    Optional<Game> optionalGame = gameDBUtility.get(gameId);
    Game game = optionalGame.get(); // TODO: throw an exception if invalid
    return game.getMoveList();
  }
}
