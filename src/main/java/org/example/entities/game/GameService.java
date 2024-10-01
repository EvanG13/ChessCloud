package org.example.entities.game;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class GameService {
  MongoDBUtility<Game> gameDBUtility;

  public GameService() {
    gameDBUtility = new MongoDBUtility<>("games", Game.class);
  }

  public GameService(MongoDBUtility<Game> utility) {
    gameDBUtility = utility;
  }

  public Game getGameFromUserID(String userId) throws NotFound {
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));

    return gameDBUtility.get(filter).orElseThrow(() -> new NotFound("No game found for player"));
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
