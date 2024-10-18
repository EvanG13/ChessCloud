package org.example.entities.game;

import com.mongodb.client.model.Filters;
import java.util.List;
import org.bson.conversions.Bson;
import org.example.entities.player.Player;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class GameDbService {
  MongoDBUtility<Game> gameDBUtility;

  public GameDbService() {
    gameDBUtility = new MongoDBUtility<>("games", Game.class);
  }

  public GameDbService(MongoDBUtility<Game> utility) {
    gameDBUtility = utility;
  }

  public Game getGameFromUserID(String userId) throws NotFound {
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));

    return gameDBUtility.get(filter).orElseThrow(() -> new NotFound("No game found for player"));
  }

  public String[] getConnectedIdsOffGame(String gameId) throws NotFound {
    Game game = get(gameId);

    List<Player> players = game.getPlayers();
    Player playerOne = players.getFirst();
    Player playerTwo = players.getLast();

    return new String[] {playerOne.getConnectionId(), playerTwo.getConnectionId()};
  }

  public boolean isConnectionIdInGame(String gameId, String connectionId) throws NotFound {
    Game game = get(gameId);

    for (Player player : game.getPlayers())
      if (player.getConnectionId().equals(connectionId))
        return true;

    return false;
  }

  public void deleteGame(String gameId) {
    gameDBUtility.delete(gameId);
  }

  public Game get(String id) throws NotFound {
    return gameDBUtility.get(id).orElseThrow(() -> new NotFound("Game not found"));
  }

  public Game get(Bson filters) throws NotFound {
    return gameDBUtility.get(filters).orElseThrow(() -> new NotFound("Game not found"));
  }

  public void patch(String id, Bson filter) {
    gameDBUtility.patch(id, filter);
  }

  public void post(Game game) {
    gameDBUtility.post(game);
  }

  public void put(String id, Game game) {
    gameDBUtility.put(id, game);
  }
}
