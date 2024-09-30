package org.example.services;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.example.entities.Game;
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
}
