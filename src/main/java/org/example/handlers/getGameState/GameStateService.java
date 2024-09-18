package org.example.handlers.getGameState;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import org.bson.conversions.Bson;
import org.example.databases.MongoDBUtility;
import org.example.entities.Game;

public class GameStateService {
  MongoDBUtility<Game> gameDBUtility;

  public GameStateService() {
    gameDBUtility = new MongoDBUtility<>("games", Game.class);
  }

  public GameStateService(MongoDBUtility<Game> utility) {
    gameDBUtility = utility;
  }

  public Optional<Game> getGame(String userId) {
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));
    return gameDBUtility.get(filter);
  }
}
