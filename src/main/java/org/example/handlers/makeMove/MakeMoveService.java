package org.example.handlers.makeMove;

import com.github.bhlangonijr.chesslib.Board;
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

  public boolean makeMove(String move, String boardState, String gameId) {
    board.loadFromFen(boardState);

    if (board.doMove(move)) {
      return false;
    }

    gameDBUtility.patch(gameId, Updates.set("gameStateAsFen", board.getFen()));

    return true;
  }
}
