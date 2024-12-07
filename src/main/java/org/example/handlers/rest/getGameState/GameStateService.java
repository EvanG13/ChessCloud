package org.example.handlers.rest.getGameState;

import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;

import org.example.entities.game.Game;
import org.example.entities.game.GameUtility;
import org.example.exceptions.NotFound;

public class GameStateService {
  private final GameUtility gameUtility;

  public GameStateService() {
    gameUtility = new GameUtility();
  }

  public GameStateService(GameUtility gameUtility) {
    this.gameUtility = gameUtility;
  }

  public Game getGameFromUserID(String userId) throws NotFound {
    return gameUtility
        .get(elemMatch("players", eq("playerId", userId)))
        .orElseThrow(() -> new NotFound("No game found for player"));
  }
}
