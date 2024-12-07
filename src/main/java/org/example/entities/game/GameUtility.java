package org.example.entities.game;

import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;

import org.bson.conversions.Bson;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class GameUtility extends MongoDBUtility<Game> {
  public GameUtility() {
    super("games", Game.class);
  }

  public GameUtility(String collection) {
    super(collection, Game.class);
  }

  public Game getGameFromUserId(String userId) throws NotFound {
    return get(elemMatch("players", eq("playerId", userId)))
        .orElseThrow(() -> new NotFound("No game found for player"));
  }

  public Game getGame(String id) throws NotFound {
    return get(id).orElseThrow(() -> new NotFound("No Game found"));
  }

  public Game getGame(Bson filters) throws NotFound {
    return get(filters).orElseThrow(() -> new NotFound("Game not found"));
  }
}
